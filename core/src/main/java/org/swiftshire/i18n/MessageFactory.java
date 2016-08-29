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

package org.swiftshire.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftshire.i18n.annotation.BundleType;
import org.swiftshire.i18n.annotation.Message;
import org.swiftshire.i18n.handler.MessageHandler;
import org.swiftshire.i18n.locale.LocaleManager;
import org.swiftshire.i18n.annotation.ResourceBundle;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates message handlers of type {@link Messages} to perform l10n.
 *
 * Message handlers must follow certain rules in order to be accepted.
 *
 * @author jswift
 * @since 1.0
 * @see Messages
 */
public class MessageFactory {
    /**
     * Logging handle
     */
    private static final Logger log = LoggerFactory.getLogger(MessageFactory.class);

    /**
     * Empty string literal
     */
    private static final String EMPTY = "";

    /**
     * Internal message (handlers) cache.
     */
    static final Map<String, Messages> handlers = new ConcurrentHashMap<>();

    /**
     * Flag that controls whether bundles can be fabricated on the fly when there is
     * no legitimate property bundle found by {@code java.util.ResourceBundle#getBundle()}.
     */
    static final AtomicBoolean doBundleFabrication = new AtomicBoolean(true);

    /**
     * Missing bundle counter to track how many missing bundles there are.
     */
    static final AtomicInteger missingBundleCount = new AtomicInteger();

    /**
     * Fabricated bundle counter to track how often a bundle had to be fabricated.
     */
    static final AtomicInteger fabricatedBundleCount = new AtomicInteger();

    /**
     * Register our I18n MBean. We use a standard MBean here in lieu of our POJO
     * approach available in the {@code com.netwitness.malware.tools.common.management} package
     * because we don't want to pull in other run time dependencies for such a simple
     * MBean and one merely for the I18n package. We're going for very lightweight here.
     */
	static {
		try {
			MBeanServer mbeanServer = locateMBeanServer();

			mbeanServer.registerMBean( new I18nService(), ObjectName.getInstance(
					"org.swiftshire.j18n:type=i18n,name=I18nService"));
		} 
		catch (InstanceAlreadyExistsException ignore) {
		} 
		catch (Exception ex) {
			log.warn("Failed to register I18nService MBean; ignoring.");

			if (log.isDebugEnabled()) {
				log.debug("I18n MBean registration failure", ex);
			}
		}
	}

    protected MessageFactory() {}

    /**
     * Controls whether to dynamically fabricate resource bundles for messages that are not
     * backed by property bundles on the filesystem. Fabrication should be avoided in all cases
     * outside of testing so this default to 'false' and is package private as a result.
     *
     * @param yesno enable or disable bundle fabrication
     */
    static void setFabricateBundles(boolean yesno) {
        doBundleFabrication.set(yesno);
    }

    /**
     * Controls whether to dynamically fabricate resource bundles for messages that are not
     * backed by property bundles on the filesystem. Fabrication should be avoided in all cases
     * outside of testing so this default to 'false' and is package private as a result.
     *
     * @return true if this factory will fabricate a resource bundle when none are found on the filesystem
     */
    static boolean getFabricateBundles() {
        return doBundleFabrication.get();
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param language language code of the locale
     */
    public static void setThreadLocale(String language) {
        LocaleManager.getManager().setThreadLocale( new Locale(language) );
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param language language code of the locale
     * @param country  country code of the locale
     */
    public static void setThreadLocale(String language, String country) {
        LocaleManager.getManager().setThreadLocale( new Locale(language, country) );
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param language language code of the locale
     * @param country country code of the locale
     * @param variant variant code of the locale
     */
    public static void setThreadLocale(String language, String country, String variant) {
        LocaleManager.getManager().setThreadLocale( new Locale(language, country, variant) );
    }

    /**
     * Set the locale with the language and country for the
     * current thread.
     *
     * @param locale locale to set
     */
    public static void setThreadLocale(Locale locale) {
        LocaleManager.getManager().setThreadLocale(locale);
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param language language code of the locale
     */
    public static void setLocale(String language) {
        LocaleManager.getManager().setLocale( new Locale(language) );
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param language language code of the locale
     * @param country  country code of the locale
     */
    public static void setLocale(String language, String country) {
        LocaleManager.getManager().setLocale( new Locale(language, country) );
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param language language code of the locale
     * @param country  country code of the locale
     * @param variant variant code of the locale
     */
    public static void setLocale(String language, String country, String variant) {
        LocaleManager.getManager().setLocale( new Locale(language, country, variant) );
    }

    /**
     * Set globally the locale with the language and country
     *
     * @param locale locale to set
     */
    public static void setLocale(Locale locale) {
        LocaleManager.getManager().setLocale(locale);
    }

    /**
     * Return currently used locale, either for thread or global.
     *
     * @return currently used locale
     */
    public static Locale getLocale() {
        return LocaleManager.getManager().getLocale();
    }

    /**
     * Parses the locale string, which must be in the form of "language_country_variant"
     * where country and variant are optional, and returns that locale.
     *
     * @param localeString the locale string in the form of
     * "language_country_variant"
     *
     * @return the true locale
     */
    private static Locale parseLocaleString(String localeString) {

        String[] localeComponents = localeString.split("_");

        String language = localeComponents.length > 0 ? localeComponents[0] : "";
        String country  = localeComponents.length > 1 ? localeComponents[1] : "";
        String variant  = localeComponents.length > 2 ? localeComponents[2] : "";

        return new Locale(language, country, variant);
    }

    /**
     * Expands messages with locale information in various ways.
     *
     * @param name
     * @param locale
     * @param suffix
     * @return
     */
    public static List<String> expand(String name, Locale locale, String suffix) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }

        if (locale == null) {
            throw new IllegalArgumentException("Locale must not be null");
        }

        if (suffix == null) {
            throw new IllegalArgumentException("Suffix must not be null");
        }

        List<String> names = new ArrayList<>();

        StringBuilder buffer = new StringBuilder(name);
        String language = locale.getLanguage();
        String country  = locale.getCountry();
        String variant  = locale.getVariant();

        names.add(name + suffix);

        if (!EMPTY.equals(language)) {
            buffer.append("_");
            buffer.append(language);
            names.add(buffer.toString() + suffix);
        }

        if (!EMPTY.equals(country)) {
            buffer.append("_");
            buffer.append(country);
            names.add(buffer.toString() + suffix);
        }

        if (!EMPTY.equals(variant)) {
            buffer.append("_");
            buffer.append(variant);
            names.add(buffer.toString() + suffix);
        }

        Collections.reverse(names);

        return names;
    }

    /**
     * Creates a resource bundle based on the {@link Message} annotations defined on the given
     * class. This is ideal for testing and in situations where the default locale matches the locale
     * of the developer however it's not recommended for production since the messages cannot be localized by
     * an external l10n team and, since we're bypassing {@code java.util.ResourceBundle#getBundle()},
     * we cannot set the locale the resulting bundle. For these reasons it's better to use the Maven i18n
     * plugin to generate a property-based resource bundle from the annotated code instead.
     *
     * @param clazz class to derive a resource bundle from.
     * @return Dynamically created resource bundle
     */
    private static <T extends Messages> ListResourceBundle fabricateBundle(Class<T> clazz)
    {
        final ArrayList<Object[]> messages = new ArrayList<>();

        Locale bundleLocale = null;

        // Check for resource bundle settings
        if (clazz.isAnnotationPresent(ResourceBundle.class)) {
            ResourceBundle bundle = clazz.getAnnotation(ResourceBundle.class);

            String loc = bundle.locale();

            if (loc != null) {
                bundleLocale = parseLocaleString(loc);
            }
        }

        if (bundleLocale == null){
            bundleLocale = Locale.getDefault();
        }

        // Set the final locale for this fabricated bundle
        final Locale defaultLocale = bundleLocale;

        // Process messages directly from the interface...
        for (Method method : clazz.getMethods()) {

            Message annotation = method.getAnnotation(Message.class);

            if (annotation != null) {
                final String key = annotation.key();
                final String message = annotation.value();

                if (message != null && message.length() > 0) {

                    Object[] entry = new Object[] { key.isEmpty() ? method.getName() : key, message };

                    messages.add(entry);
                }
            }
        }

        fabricatedBundleCount.incrementAndGet();

        return new ListResourceBundle() {
            final Object[][] contents = messages.toArray( new Object[0][] );

            protected Object[][] getContents() {
                return contents;
            }

            public Locale getLocale() {
                return defaultLocale;
            }
        };
    }

    /**
     * Creates a new messages object to use for localized messages.
     *
     * @param clazz An interface that extends {@link Messages}
     * @return New localized messages object suitable for the current locale
     */
    public static <T extends Messages> T create(Class<T> clazz) {
        return create(clazz, getLocale());
    }

    /**
     * Creates a new messages object to use for localized messages.
     *
     * @param clazz An interface that extends {@link Messages}
     * @param localeString an ISO locale string such as "en_US" or "en_UK", etc.
     * @return An instance of the given class type the caller can use to localize strings
     */
    public static <T extends Messages> T create(Class<T> clazz, String localeString) {

        Locale locale;

        if (localeString != null) {
            locale = parseLocaleString(localeString);
        }
        else {
            locale = getLocale();
        }

        return create(clazz, locale);
    }

    /**
     * Creates a new messages object to use for localized messages.
     *
     * @param clazz An interface that extends {@link Messages}
     * @param locale A target locale that identifies a specific resouce bundle
     * @return An instance of the given class type the caller can use to localize strings
     */
    @SuppressWarnings("unchecked")
	public static <T extends Messages> T create(Class<T> clazz, Locale locale) {
        if (clazz == null) {
            throw new IllegalArgumentException("Messages type must not be null");
        }

        if (!clazz.isInterface()) {
            throw new UnsupportedOperationException("Types derived from Messages must be an interface!");
        }

        // Has the user overridden the default behavior for resource bundles?
        String bundleName = clazz.getName();

        ResourceBundle bundle = clazz.getAnnotation(ResourceBundle.class);

        if (bundle != null) {
            // Have they specified a bundle name?
            String name = bundle.name();

            if (name != null && name.length() > 0) {
                bundleName = name;
            }
        }

        final String cacheId = bundleName + "_" + locale;

        Messages messages;

        synchronized (handlers) {
            messages = handlers.get(cacheId);

            if (messages == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating new message handler for bundle " +
                            bundleName.substring( bundleName.lastIndexOf('.') + 1 ) + "_" + locale);
                }

                // Create the proper invocation message handler
                InvocationHandler handler;

                try {
                    handler = new MessageHandler(bundleName, locale, clazz.getClassLoader());
                }
                catch (MissingResourceException ex) {

                    missingBundleCount.incrementAndGet();

                    // Do we fabricate the bundle or punt?
                    if ((bundle != null && bundle.type() == BundleType.NONE) || doBundleFabrication.get()) {

                        log.warn(ex.getMessage() + "; fabricating bundle dynamically.");

                        handler = new MessageHandler( fabricateBundle(clazz) );
                    }
                    else {
                        throw ex;
                    }
                }

                messages = (Messages) Proxy.newProxyInstance(
                                            clazz.getClassLoader(),
                                            new Class[] { clazz },
                                            handler);

                handlers.put(cacheId, messages);
            }
        }

        return (T) messages;
    }
    
	/**
	 * Attempt to find a locally running {@code MBeanServer}. Fails if no
	 * {@code MBeanServer} can be found. Logs a warning if more than one {@code
	 * MBeanServer} found, returning the first one from the list.
	 * 
	 * @return the {@code MBeanServer} if found
	 * @throws RuntimeException
	 *             if no {@code MBeanServer} could be found
	 * @see javax.management.MBeanServerFactory#findMBeanServer(String)
	 */
	static MBeanServer locateMBeanServer() {
		ArrayList<MBeanServer> servers = MBeanServerFactory.findMBeanServer(null);

		MBeanServer server = null;
		
		if (servers != null && servers.size() > 0) {
			// Check to see if an MBeanServer is registered.
			if (servers.size() > 1 && log.isWarnEnabled()) {
				log.warn("Found more than one MBeanServer instance. Returning first from list.");
			}
			
			server = servers.get(0);
		}

		if (server == null) {
			// Attempt to load the PlatformMBeanServer.
			try {
				server = ManagementFactory.getPlatformMBeanServer();
			} 
			catch (SecurityException ex) {
				throw new RuntimeException(
                        "No specific MBeanServer found, and not allowed to obtain the Java platform MBeanServer", ex);
			}
		}

		if (server == null) {
			throw new RuntimeException("Unable to locate an MBeanServer instance.");
		}

		if (log.isDebugEnabled()) {
			log.debug("Found MBeanServer: " + server);
		}

		return server;
	}
}
