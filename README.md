# JI18n Project

## Internationalization Framework for Java Applications

Internationalization, often abbreviated using the numeronym "i18n" (where 18 stands for the number of letters between 
the first 'i' and the last 'n' in the word “internationalization,” a usage coined at DEC in the '70s / '80s) is the 
process of developing code in such a way that it is completely independent of any culture-specific information. Fully 
internationalized applications can be localized to a particular country or region without any changes in the program 
code. An application that supports i18n will have the following characteristics:
                                                                                                             
* No code changes are required to support additional languages.
* Text messages, and icons and images containing text elements, are stored externally to the source code.
* Locale sensitive data such as dates, times, numbers, and currencies are formatted according to the user’s language and geographic region.
* Nonstandard character sets are supported.
* The application is engineered in such a way that it can be rapidly adapted—or localized—to new languages or regions.

JI18n is an easy to use framework to simplify this process for Java applications. At a high level, JI18n effectively 
adds a support layer on top of the existing i18n capabilities of the standard Java JDK / JRE by providing a tiny core 
runtime and Maven and Gradle plugins to that produce [java.util.ResourceBundle](https://docs.oracle.com/javase/7/docs/api/java/util/PropertyResourceBundle.html) backed properties files (e.g. `MyResources_en_US` or `MyResources_de`) 
as a function of your build process by scanning your code for JI18n annotated messages and, instead, enabling you to use 
coded method names as identifiers for those textual messages in your application effectively externalizing those strings 
from your code entirely for you. 

### Example 

#### Source Code Walkthrough

**Step 1** : Create your i18n messages by extending the JI18n `Messages` interface and adding a method for each individual message 
you need. It's recommended that you use fairly lengthy and descriptive method names to identify their corresponding i18n strings. 
Note that methods *cannot* be overloaded so the names that you use must be *unique*!

```Java
import org.swiftshire.i18n.Messages;
import org.swiftshire.i18n.annotation.Message;

public interface TestMessages extends Messages {

    @Message("This is an English welcome message.")
    String welcome();

    @Message("Hello {0}. You visited this website {1,number} times.")
    String hello(String name, int count);
}
```

**Step 2** : Use these messages anywhere in your code via a static `MessageFactory` object like so:

```Java
import org.swiftshire.i18n.MessageFactory;

public class Example {
    private static final TestMessages i18n = MessageFactory.create(TestMessages.class);

    pubic static void main() {
        // Prints the following to STDOUT: "Hello User. You visited this website 1 times"
        System.out.println( i18n.hello("User", 1) );
    }
}
```

That's it! That is the minimum necessary to start using this framework to internationalize your strings. However, at some point 
you will want to package up these strings in a separate Java JDK resource bundle properties file(s) that you can hand over to a 
localization team or company to localize for whatever target locale you wish to support. This is where the build plugin comes in.
 
#### Maven Plugin Walkthrough 

**Step 1** : Add the following dependency to the `<dependencies/>` section of your POM:

```
<dependency>
  <groupId>org.swiftshire</groupId>
  <artifactId>ji18n-core</artifactId>
  <version>1.0</version>
<dependency>
```

**Step 2** : Add the following plugin definition to the `<plugins/>` section of your POM:

```
<plugin>
    <groupId>org.swiftshire</groupId>
    <artifactId>ji18n-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
That's it! This will instruct the J18n Maven plugin to scan your source code for all `Messages` definitions and automatically
generate a corresponding Java resource bundle properties file for each respectively and place them in the correct location on your build
path so they can be loaded from the resulting JAR / classpath at run time when your Java application executes.

### Advantages 

This framework offers a number of compelling advantages including:

1. All natural language strings are externalized away from your source code as resource bundles automatically (no manual editing).
1. There is a degree of type safety in arguments that are interpolated within localized strings since methods are used.
1. IDE refactoring can be used to manage your internationalized strings in your project since you're calling type-safe methods for message identifiers and parameter interpolation.
1. Statistics and dynamic management are exposed to the JMX runtime using a JI18n MBean.

    
## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/swiftj/ji18n/issues).

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.