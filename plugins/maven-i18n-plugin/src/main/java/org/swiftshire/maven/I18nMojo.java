/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.swiftshire.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.swiftshire.i18n.Messages;
import org.swiftshire.i18n.annotation.BundleType;
import org.swiftshire.i18n.annotation.Message;
import org.swiftshire.i18n.annotation.Permission;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import org.swiftshire.i18n.annotation.ResourceBundle;

import javax.management.DynamicMBean;
import javax.management.MXBean;

/**
 * Mojo for generating resource bundles from annotated source code.
 *
 * @goal generate
 * @threadSafe
 */
public class I18nMojo extends AbstractMojo {
    /**
     * The Maven project object
     *
     * @parameter property="${project}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private MavenProject project;

    /**
     * The basedir of the project.
     *
     * @parameter property="${basedir}"
     * @required
     * @readonly
     */
    protected File basedir;

    /**
     * This is where everything is built.
     *
     * @parameter property="${project.build.directory}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private File buildDirectory;

    /**
     * This is where compiled classes go.
     *
     * @parameter property="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File outputDirectory;

    /**
     * This is where compiled test classes go.
     *
     * @parameter property="${project.build.testOutputDirectory}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private File testOutputDirectory;

    /**
     * Flag to indicate that this task should log some debug messages during processing.
     *
     * @parameter property="false"
     */
    protected boolean verbose = false;

    /**
     * Flag to indicate that this task should verify the correctness of the bundles and messages to be generated.
     *
     * @parameter property="true"
     */
    protected boolean verify = true;

    /**
     * If <code>true</code>, the messages will be appended to the end of the resource bundle files if they already
     * exist. If <code>false</code>, the resource bundle files that do exist will be overwritten.
     *
     * @parameter
     */
    private boolean append = false;

    /**
     * Flag to indicate that strict enforcement of i18n messages be done.
     *
     * @parameter property="false"
     */
    protected boolean pedantic = false;

    /**
     * Flag to indicate that all messages should be aggregated as well
     *
     * @parameter property="false"
     */
    protected boolean aggregate = false;

    /**
     * The fully-qualified name of the aggregate message bundle to create
     *
     * @parameter property="Messages"
     */
    protected String aggregateName = "Messages";

    /**
     * If not <code>null</code>, this is the locale that will be used as the default locale for the application. If a
     * resource bundle file is generated with this locale, that resource bundle file will be copied under a filename
     * which is just the base bundle name, excluding a language/country/variant.
     */
    private Locale defaultLocale = Locale.getDefault();

    /**
     * Used for verifying - this will contain all the keys generated in all the bundles so we can detect duplicates.
     */
    private Map<ResourceBundleDefinition, List<String>> generatedBundleMessageKeys;

    /**
     * Map that contains counters of the number of messages in each generated bundle for reporting purposes at the end
     * of the task's execution. This is also useful for determine what resource bundles were generated.
     */
    private Map<ResourceBundleDefinition, Integer> bundleCounters;

    /**
     * Resource bundle that stores the internationalized descriptions of permissions.
     */
    private ResourceBundleDefinition permissionsDefinition;

    /**
     * Resource bundle that stores the aggregate messages (if enabled).
     */
    private ResourceBundleDefinition aggregateDefinition;

    /**
     * @throws org.apache.maven.plugin.MojoExecutionException
     *
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    public void execute() throws MojoExecutionException {
        getLog().info("Scanning for i18n messages...");

        permissionsDefinition =
                new ResourceBundleDefinition(outputDirectory + File.separator + Permission.RESOURCE_FILE);

        if (aggregate) {
            if (aggregateName == null || "".equals(aggregateName)) {
                throw new MojoExecutionException("aggregateName must be the fully qualified name of the message bundle");
            }

            String path = aggregateName.replace(".", File.separator);
            aggregateDefinition =
                    new ResourceBundleDefinition(outputDirectory + File.separator + path);
        }

        if (verify) {
            generatedBundleMessageKeys = new HashMap<>();
        }

        // We'll need a custom class loader to process each class
        ClassLoader classloader;

        try {
            classloader = new URLClassLoader(
                    new URL[]{outputDirectory.toURL()},
                    this.getClass().getClassLoader());

            Thread.currentThread().setContextClassLoader(classloader);
        }
        catch (MalformedURLException ex) {
            throw new MojoExecutionException(ex.getMessage());
        }

        // Next we need to scan the Maven target directory for all the classes
        String[] includes = {"**\\*.class"};
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(includes);
        scanner.setBasedir(outputDirectory);
        scanner.setCaseSensitive(true);
        scanner.scan();

        // Now load each class file and process it
        bundleCounters = new HashMap<>();

        for (String classFile : scanner.getIncludedFiles()) {
            try {
                // Strip file extension
                classFile = classFile.substring(0, classFile.lastIndexOf('.'));

                // Convert file path to class path then load the class itself.
                Class clazz = classloader.loadClass(
                        classFile.replace(File.separatorChar, '.'));

                if (!processClass(clazz, classFile)) {
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("The class [" + clazz +
                                "] does not appear to be annotated for i18n and was not processed");
                    }
                }
            }
            catch (NoClassDefFoundError ignore) {
                // Ignore classes that we did not build ourselves.
            }
            catch (ClassNotFoundException ex) {
                throw new MojoExecutionException(
                        "Cannot find class file: " + ex.getMessage());
            }
        }

        // Now make copies of the resource bundles so the default locale is used in non-supported locales
        // To avoid concurrent modifications to bundleCounters, store in here the new, default bundles we create
        Map<ResourceBundleDefinition, Integer> defaultBundles = new HashMap<>();

        for (Map.Entry<ResourceBundleDefinition, Integer> counter : bundleCounters.entrySet()) {

            ResourceBundleDefinition bundle = counter.getKey();

            if (defaultLocale.equals(bundle.locale)) {
                // Generated bundle is one that contains messages in the same locale 
                // as the default locale. Copy that bundle ("baseName_locale.properties") 
                // to a default bundle ("baseName.properties")
                ResourceBundleDefinition defaultBundle = new ResourceBundleDefinition(bundle.baseName);
                defaultBundle.locale = null;

                copyResourceBundle(bundle, defaultBundle);

                Integer count = counter.getValue();

                defaultBundles.put(defaultBundle, count);
            }
        }

        // Since we've created more bundles, let's add them to our running counter
        bundleCounters.putAll(defaultBundles);

        // We are done generating the files - let's log a report of what we did
        if (bundleCounters.size() > 0) {
            if (verbose) {
                getLog().info("The following i18n resource bundles were generated\n");

                for (Map.Entry<ResourceBundleDefinition, Integer> entry : bundleCounters.entrySet()) {
                    getLog().info("-> " + entry.getKey() + ": " + entry.getValue() + " messages");
                }
            }
            else {
                int bundles = 0;
                int messages = 0;

                for (Integer counter : bundleCounters.values()) {
                    ++bundles;
                    messages += counter;
                }

                getLog().info("" + bundles +
                        " resource bundle(s) created with " + messages + " message(s) in total.");
            }
        }
        else {
            getLog().info("No resource bundles generated because no i18n messages were found.");
        }
    }

    /**
     * Processes the i18n annotations found on the given class and its methods. Returns <code>true</code> if the given
     * class was annotated in such a way as to indicate it is internationalized and was processed by this task.
     * <code>false</code> is returned if the class did not have any i18n annotations.
     *
     * @param clazz the clazz to process
     * @param classFile
     * @return <code>true</code> if this class was processed by this task, <code>false</code> if this class did not have
     *         any i18n information annotated on it
     * @throws MojoExecutionException if the class was inappropriately annotated with the i18n annotations
     */
    private boolean processClass(Class<?> clazz, String classFile) throws MojoExecutionException {

        boolean internationalized = false;

        // Only specific interfaces can be message bundles.
        if (clazz.isInterface()) {
            if (Messages.class.isAssignableFrom(clazz) || JmxHelper.isMBean(clazz)) {

                // This defines the default resource bundle for the given class
                ResourceBundleDefinition bundleDef =
                        new ResourceBundleDefinition(outputDirectory + File.separator + classFile);

                // Process the top class-level resource bundle annotation, if one exists.
                // This will be the default resource bundle definition for all messages defined on the class's methods.
                if (clazz.isAnnotationPresent(ResourceBundle.class)) {

                    ResourceBundle annotation = clazz.getAnnotation(ResourceBundle.class);

                    if (annotation.type() == BundleType.NONE) {
                        // Skip this altogether
                        return false;
                    }

                    bundleDef.setLocale(annotation.locale());
                    bundleDef.setBaseName(annotation.name());
                    bundleDef.setType(annotation.type());

                    internationalized = true;

                    // Check for statically defined messages to include
                    String[] staticMessages = annotation.define();

                    if (staticMessages != null) {
                        for (String msg : staticMessages) {
                            try {
                                int delim = msg.indexOf('=');

                                String key = msg.substring(0, delim++);
                                String val = msg.substring(delim);

                                writeMessage(clazz, bundleDef, key, val);
                                if (aggregate) {
                                    writeMessage(clazz, aggregateDefinition, key, val);
                                }
                            }
                            catch (MojoExecutionException ex) {
                                throw ex;
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                                throw new MojoExecutionException(
                                        "Failed to write statically defined i18n message to resource bundle for  [" +
                                                clazz.getName() + " : " + msg + "]");
                            }
                        }
                    }

                    if (getLog().isDebugEnabled()) {
                        getLog().debug(clazz.getName() +
                                " is annotated with resource bundle definition " + bundleDef);
                    }
                }

                // Generate the bundle now
                for (Method method : clazz.getMethods()) {

                    final Message annotation = method.getAnnotation(Message.class);

                    if (annotation != null) {
                        final String key = annotation.key();

                        // All message methods *must* return 'java.lang.String'
                        if (method.getReturnType().equals(String.class)) {
                            try {
                                writeMessage(clazz, bundleDef, key.isEmpty() ? method.getName() : key, annotation.value());

                                if (aggregate) {
                                    writeMessage(clazz, aggregateDefinition, key.isEmpty() ? method.getName() : key, annotation.value());
                                }
                            }
                            catch (MojoExecutionException ex) {
                                throw ex;
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                                throw new MojoExecutionException(
                                        "Failed to write i18n message to resource bundle for  [" +
                                                clazz.getName() + "." + method.getName() + "]");
                            }

                            internationalized = true;
                        }
                        else {
                            getLog().warn("Annotated method [ " + clazz.getSimpleName() + "." +
                                    method.getName() + " ] must return String! Ignoring message.");
                        }
                    }
                    else {
                        // Method not annotated with @Message
                    }
                }
            }

            // Check interface constants
            for (Field field : clazz.getDeclaredFields()) {

                final Permission permission = field.getAnnotation(Permission.class);

                if (permission != null) {
                    final String value = permission.value();

                    if (field.getType().equals(String.class)) {
                        // Since the field is off an interface, we know it must be static, so no instance
                        // object is required
                        try {
                            String key = (String) field.get(null);

                            writeMessage(clazz, permissionsDefinition, key, value);

                            if (aggregate) {
                                writeMessage(clazz, aggregateDefinition, key, value);
                            }
                        }
                        catch (MojoExecutionException ex) {
                            throw ex;
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                            throw new MojoExecutionException(
                                    "Failed to write i18n permission to resource bundle for  [" +
                                            clazz.getName() + "." + field.getName() + "]");
                        }

                        internationalized = true;
                    }
                    else {
                        getLog().warn("Annotated field [ " + clazz.getSimpleName() + "." +
                                field.getName() + " ] must be declared String! Ignoring permission.");
                    }
                }
            }
        }

        return internationalized;
    }

    /**
     * Writes the i18n message to the resource bundle file. It will also generate the helpdoc item for the message, if
     * help documentation has been enabled.
     * <p/>
     * <p>If the <code>i18n_message</code>'s text is <code>null</code> or an empty string, it will be ignored and this
     * method will not write anything.</p>
     *
     * @param clazz   the class that contains the given i18n-annotated method
     * @param bundle  describes the resource bundle - use this to determine the filename of the bundle (note that the
     *                given i18n message may override the locale of this bundle with its own locale)
     * @param key     the resource bundle key of the message
     * @param message the annotation carrying the actual message
     * @throws MojoExecutionException if failed to write the message to the resource bundle file
     */
    private void writeMessage(Class<?> clazz, ResourceBundleDefinition bundle, String key, Message message)
            throws MojoExecutionException {

        writeMessage(clazz, bundle, key, message.value());
    }

    /**
     * Writes the i18n message to the resource bundle file. It will also generate the helpdoc item for the message, if
     * help documentation has been enabled.
     * <p/>
     * <p>If the <code>i18n_message</code>'s text is <code>null</code> or an empty string, it will be ignored and this
     * method will not write anything.</p>
     *
     * @param clazz  the class that contains the given i18n-annotated method
     * @param bundle describes the resource bundle - use this to determine the filename of the bundle (note that the
     *               given i18n message may override the locale of this bundle with its own locale)
     * @param key    the resource bundle key of the message
     * @param text   the actual message
     * @throws MojoExecutionException if failed to write the message to the resource bundle file
     */
    private void writeMessage(Class<?> clazz, ResourceBundleDefinition bundle, String key, String text)
            throws MojoExecutionException {

        // Make sure the message text is defined; if not, its probably a language 
        // translation placeholder so just ignore it
        if (text == null || text.trim().length() == 0) {
            getLog().warn("WARNING: The text for i18n message key [" + key +
                    "] for bundle [" + clazz.getName() + "] is empty and will be ignored");

            return;
        }

        // If verifying, make sure a newline in text is followed by an escape \
        // since most times, a newline in a bundle message should be followed by 
        // a \ character to indicate the message continues on the next line.
        if (verify) {
            checkNewlines(clazz, key, text);
            checkSingleQuotes(key, text);
            checkForDuplicate(bundle, key);
        }

        // Update our counter to indicate we are adding one more message to the 
        // bundle if this is the first time we've seen this bundle file, 
        // determine if we should append to it or not
        boolean doAppend = true;
        Integer count = bundleCounters.get(bundle);

        if (count == null) {
            count = 0;
            bundleCounters.put(bundle, count);
            doAppend = this.append;
        }

        bundleCounters.put(bundle, ++count);

        // Now write the message to the bundle file
        File bundleFile = new File(bundle + ".properties");
        String resourceKeyValue = key + "=" + text;

        getLog().debug("Writing to bundle file [" + bundleFile +
                "] the key=message of: " + resourceKeyValue);

        try (PrintWriter writer = new PrintWriter(new FileWriter(bundleFile, doAppend))) {
            writer.println(resourceKeyValue);
        }
        catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage());
        }
    }

    /**
     * This checks to see if the given <tt>bundle_key</tt> has been generated before in the same <tt>bundle</tt>. If so,
     * a simple warning is logged but nothing else is done - the task should continue normally.
     *
     * @param bundle
     * @param bundleKey
     */
    private void checkForDuplicate(ResourceBundleDefinition bundle, String bundleKey) throws MojoExecutionException {

        List<String> bundleKeyList = generatedBundleMessageKeys.get(bundle);

        if (bundleKeyList != null) {
            if (bundleKeyList.contains(bundleKey)) {
                final String msg = "Duplicate i18n message keys found (overloading not permitted) : " + bundleKey;

                if (pedantic) {
                    throw new MojoExecutionException(msg);
                }
                else {
                    getLog().error(msg);
                }
            }
        }
        else {
            bundleKeyList = new ArrayList<>();
            generatedBundleMessageKeys.put(bundle, bundleKeyList);
        }

        bundleKeyList.add(bundleKey);
    }

    /**
     * This verifies that, if the given text string contains a single quote character, that no placeholders appear
     * within the quoted portion of the string. While it is valid to have a message look something like <code>"this is
     * quoted '{0}'</code>", it is usually not going to be what the developer wants it to be. Usually, the only time
     * "{#}" strings exist in the message is because they are meant to be placeholders that are to be replaced with
     * values during runtime. However, having them within single quotes effectively creates them as literal "{#}"
     * strings and they will not get replaced (see the Javadoc on <code>java.text.MessageFormat</code> for the syntax
     * rules). This type of error usually occurs when an English-speaking developer uses apostrophes in the message, not
     * realizing that the single apostrophe has this effect (e.g. <code>"you don't want to do this because the
     * placeholder {0} is inside a single-quoted string and will not be replaced - so use the words DO NOT
     * instead"</code>). If such a condition exists in the given text string, a simple warning is logged but nothing
     * else is done - the task should continue normally.
     *
     * @param bundleKey
     * @param text
     */
    private void checkSingleQuotes(String bundleKey, String text) throws MojoExecutionException {

        int firstQuote = text.indexOf('\'');
        int secondQuote;
        boolean quotedPlaceholder = false;

        while (firstQuote > -1 && !quotedPlaceholder) {
            secondQuote = text.indexOf('\'', firstQuote + 1);

            // If the second quote is right after the first (''), this is a 
            // literal quote and will be skipped
            if ((firstQuote + 1) == secondQuote) {
                // Go to the next single quote in the text
                firstQuote = text.indexOf('\'', secondQuote + 1);
            }
            else {
                // If there is a second quote and it isn't at the last character,
                // look for a placeholder between first and second
                if (secondQuote > -1) {
                    quotedPlaceholder = text.substring(
                            firstQuote, secondQuote).matches("(?s).*\\{[0-9].*");

                    firstQuote = text.indexOf('\'', secondQuote + 1);
                }
                else {
                    // There is no second quote or its at the last position, so just
                    // look for a placeholder after the first quote
                    quotedPlaceholder = text.substring(firstQuote).matches("(?s).*\\{[0-9].*");
                    firstQuote = -1;
                }
            }
        }

        if (quotedPlaceholder) {
            final String msg = "Check single and double quotes for message key : " + bundleKey;

            if (pedantic) {
                throw new MojoExecutionException(msg);
            }
            else {
                getLog().error(msg);
            }
        }
    }

    /**
     * This simply verifies that if the given text string has newline characters, that they are all preceded by an
     * escape backslash. Most of the time, your resource bundle strings will want to escape newline characters so the
     * bundle string is continued onto the next line. If an escape backslash is not found, a simple warning is logged
     * but nothing else is done - the task should continue normally.
     *
     * @param bundleKey
     * @param text
     */
    @SuppressWarnings("unchecked")
    private void checkNewlines(Class clazz, String bundleKey, String text) throws MojoExecutionException {

        int newlineIndex = -1;

        do {
            // Assumes we'll find a \n and not a \r\n
            int nextIndexIncrement = 1;
            int startSearchIndex = newlineIndex;

            newlineIndex = text.indexOf("\r\n", startSearchIndex);

            if (newlineIndex == -1) {
                newlineIndex = text.indexOf('\n', startSearchIndex);
            }
            else {
                // Need to skip across the \r\n// need to skip across the \r\n
                nextIndexIncrement = 2;
            }

            if (newlineIndex > -1) {
                // The message text has a newline let's verify that it has an 
                // end-of-line escape character, since that's usually what you want
                if (newlineIndex == 0 || text.charAt(newlineIndex - 1) != '\\') {
                    final String msg = "Unescaped newline character in message [" + bundleKey +
                            "] found in bundle '" + clazz.getSimpleName() + "'";

                    if (pedantic) {
                        throw new MojoExecutionException(msg);
                    }
                    else {
                        getLog().error(msg);
                    }
                }

                newlineIndex += nextIndexIncrement;
            }
        } while (newlineIndex > -1);
    }

    /**
     * Copies a resource bundle properties file.
     *
     * @param from identifies the resource bundle that is to be copied
     * @param to   identifies the new resource bundle whose contents will be the same as the <code>from</code> bundle
     * @throws MojoExecutionException if failed to copy the resource bundle properties file
     */
    private void copyResourceBundle(ResourceBundleDefinition from, ResourceBundleDefinition to)
            throws MojoExecutionException {

        File fromFile = new File(from + ".properties");
        File toFile = new File(to + ".properties");

        InputStream inStream = null;
        OutputStream toStream = null;

        try {
            int size = 32768;

            inStream = new BufferedInputStream(new FileInputStream(fromFile), size);
            toStream = new BufferedOutputStream(new FileOutputStream(toFile), size);

            byte[] buffer = new byte[size];

            for (int bytes = inStream.read(buffer); bytes != -1; bytes = inStream.read(buffer)) {
                toStream.write(buffer, 0, bytes);
            }

            toStream.flush();
        }
        catch (Exception ex) {
            throw new MojoExecutionException(
                    "Failed to copy resource bundle [" + fromFile + "] to [" +
                            toFile + "]: " + ex.getMessage());
        }
        finally {
            try {
                if (toStream != null) {
                    toStream.close();
                }
            }
            catch (Exception ignored) {
            }

            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (Exception ignored) {
            }
        }
    }

    /**
     * Internal Java 6 MXBean verifier class
     */
    private static class JmxHelper {
        /**
         * Suffix used to identify an MBean interface.
         */
        private static final String MBEAN_SUFFIX = "MBean";

        /**
         * Suffix used to identify a Java 6 MXBean interface.
         */
        private static final String MXBEAN_SUFFIX = "MXBean";

        /**
         *
         * @param iface
         * @return
         */
        public static Boolean evaluateMXBeanAnnotation(Class<?> iface) {
            MXBean mxBean = iface.getAnnotation(MXBean.class);
            return (mxBean != null ? mxBean.value() : null);
        }

        /**
         * Determine whether the given bean class qualifies as an MBean as-is.
         * <p>This implementation checks for {@link javax.management.DynamicMBean}
         * classes as well as classes with corresponding "*MBean" interface
         * (Standard MBeans) or corresponding "*MXBean" interface (Java 6 MXBeans).
         *
         * @param clazz the bean class to analyze
         * @return whether the class qualifies as an MBean
         */
        public static boolean isMBean(Class<?> clazz) {
            return (clazz != null &&
                    (DynamicMBean.class.isAssignableFrom(clazz) ||
                            (getMBeanInterface(clazz) != null || getMXBeanInterface(clazz) != null)));
        }

        /**
         * Return the Standard MBean interface for the given class, if any
         * (that is, an interface whose name matches the class name of the
         * given class but with suffix "MBean").
         *
         * @param clazz the class to check
         * @return the Standard MBean interface for the given class
         */
        public static Class<?> getMBeanInterface(Class<?> clazz) {
            if (clazz == null || clazz.getSuperclass() == null) {
                return null;
            }

            String mbeanInterfaceName = clazz.getName() + JmxHelper.MBEAN_SUFFIX;

            Class[] implementedInterfaces = clazz.getInterfaces();

            for (Class<?> iface : implementedInterfaces) {
                if (iface.getName().equals(mbeanInterfaceName)) {
                    return iface;
                }
            }

            return getMBeanInterface(clazz.getSuperclass());
        }

        /**
         * Return the Java 6 MXBean interface exists for the given class, if any
         * (that is, an interface whose name ends with "MXBean" and/or
         * carries an appropriate MXBean annotation).
         *
         * @param clazz the class to check
         * @return whether there is an MXBean interface for the given class
         */
        public static Class<?> getMXBeanInterface(Class<?> clazz) {
            if (clazz == null || clazz.getSuperclass() == null) {
                return null;
            }

            Class[] implementedInterfaces = clazz.getInterfaces();

            for (Class<?> iface : implementedInterfaces) {
                boolean isMxBean = iface.getName().endsWith(JmxHelper.MXBEAN_SUFFIX);

                Boolean checkResult = JmxHelper.evaluateMXBeanAnnotation(iface);

                if (checkResult != null) {
                    isMxBean = checkResult;
                }

                if (isMxBean) {
                    return iface;
                }
            }

            return getMXBeanInterface(clazz.getSuperclass());
        }
    }

    /**
     * This is a simple object to encapsulate a resource bundle's base name and locale.
     */
    private class ResourceBundleDefinition {

        private static final String DEFAULT_NAME = "Messages";

        private String baseName = DEFAULT_NAME;
        private Locale locale;
        private BundleType type;

        public ResourceBundleDefinition(String baseName) {
            this.baseName = baseName;
            this.type = BundleType.PROPERTY;
            this.locale = defaultLocale != null ? defaultLocale : Locale.getDefault();
        }

        @SuppressWarnings("unused")
        public String getBaseName() {
            return baseName;
        }

        public void setBaseName(String baseName) {
            if (baseName != null && baseName.length() > 0) {
                this.baseName = baseName;
            }
        }

        @SuppressWarnings("unused")
        public Locale getLocale() {
            return locale;
        }

        @SuppressWarnings("unused")
        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        public void setLocale(String localeString) {
            if (localeString != null && localeString.length() > 0) {
                this.locale = parseLocaleString(localeString);
            }
        }

        @SuppressWarnings("unused")
        public BundleType getType() {
            return type;
        }

        public void setType(BundleType type) throws MojoExecutionException {
            // We currently only support property file bundles. In the future
            // we should extend this plugin to support XLIFF and Properties XML.
            if (type != BundleType.PROPERTY) {
                throw new MojoExecutionException(
                        "Unsupported bundle type for " + baseName + ": " + type);
            }

            this.type = type;
        }

        /**
         * Parses the locale string, which must be in the form of "language_country_variant" where country and variant
         * are optional, and returns that locale.
         *
         * @param localeString the locale string in the form of "language_country_variant"
         * @return the true locale
         */
        private Locale parseLocaleString(String localeString) {

            String[] localeComponents = localeString.split("_");

            String language = localeComponents.length > 0 ? localeComponents[0] : "";
            String country = localeComponents.length > 1 ? localeComponents[1] : "";
            String variant = localeComponents.length > 2 ? localeComponents[2] : "";

            return new Locale(language, country, variant);
        }

        /**
         * @see Object#toString()
         */
        @Override
        public String toString() {
            return baseName + ((locale != null) ? ("_" + locale) : (""));
        }

        /**
         * @see Object#equals(Object)
         */
        @Override
        public boolean equals(Object obj) {
            return this.toString().equals(obj.toString());
        }

        /**
         * @see Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
    }
}
