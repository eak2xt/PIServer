
About 
=========

A Server for hosting PIPlayer Trials.  Please see (https://github.com/ProjectImplicit/PIPlayer)
This server provides:

* User Authentication
* Participant Assignments
* Data Collection
* Data Reporting
* Session management

It is built in Java using the Spring Framework and Spring Boot.  There is exceptional documentation
on these technologies here:  http://spring.io/guides#gs


Getting Started
===============

Requirements
---------------
You must have the following applications installed in order to build and run the server.
* Java 7 JDK - (http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* Node (Server side Javascript - for building the PIPlayer, see http://howtonode.org)
* Bower (Javascript package management tool - just run "npm install bower -global")
* Mysql (Relational Database - http://dev.mysql.com/doc/refman/5.1/en/installing.html)

Database Setup
---------------
Install MySQL, and execute the following commands to establish
a user account.  You can use a different password if you change
the datasource.password setting in src/main/resources/application.properties

> CREATE database pi;
> CREATE USER 'pi_user'@'localhost' IDENTIFIED BY 'pi_password';
> GRANT ALL PRIVILEGES ON pi.* TO 'pi_user'@'localhost' IDENTIFIED BY 'pi_password' WITH GRANT OPTION;

Installing Javascript Dependencies
-------------------
Javascript dependencies, including the PIPlayer are installed using Bower, just run `bower install`

Please Note:  if you run into problems with PI Player scripts not executing you might try editing the file
/PIServer/src/main/resources/static/PIPlayer/dist/js/config.js
Set the baseUrl:'../PIPlayer/dist/js',

Because of the way the PIPlayer script is currently designed, you will need to install the PIPlayer dependencies
manually,  you can do this by:

> cd src/main/resources/static/bower/PIPlayer
> bower install

Running
--------
You can start up the webserver in development mode (meaning hot swappable / auto reloading) with:
$ ./gradlew clean bootrun
(on windows this is ./gradlew.bat clean bootrun)

You can now visit the website at : http://localhost:9000

Deploying
--------
You can generate a WAR file suitable for deployment in a web server with 
$ ./gradlew war
Then you will find the war file in ./build/lib/piServer-0.1.0.war

If you need to modify the default configuration for production (and you most likely will) then you should edit
the file WEB-INF/classes/application.properties that exists within the WAR file (just edit the file in place)
There are three major areas to configure for a production installation:
1. You will need to create/update/modify permissions on a database.
1. You will need an account with Tango (a service to automate the giving of gift cards.)

A word on Sensitive Data
======================
It is critical that we keep the link between a participants personal medical history and
their identifiable information (such as email) separate.  It is also important that we
be able to re-connect this information in the event that we need to contact the particpant
because we identify a pattern in the data collected that we much ethically notify the
participant about.
To keep participant data anonymous (but ultimately linkable) there is the ability to export
and then expunge this information from the main web server.
A series of REST endpoints exist that allow for listing all data that is available, downloading
that data, and then removing the data from the server. These end points include:

**GET** *[SERVER]/api/export*:  Returns a list of all questionnaires, including the number of records, and if the records can and should be deleted after export.
For example: *curl -u admin@email.com:passwd localhost:9000/api/export* would return:
```javascript
[
   {
      "name" : "SUDS",
      "deleteable" : true,
      "size" : 9
   },
   {
      "name" : "DASS21_DS",
      "deleteable" : true,
      "size" : 16
   },
   {
      "name" : "Demographic",
      "deleteable" : true,
      "size" : 4
   }
   ...
]
   
```
**GET** *SERVER/api/export/NAME*:  Returns all the data available on the server at that moment.
For example: *curl -u admin@email.com:passwd localhost:9000/api/export/ImageryPrime* would return:
```javascript
[
   {
      "id" : 1,
      "vivid" : 0,
      "situation" : "Waiting at doctors office",
      "prime" : "prime",
      "think_feel" : null,
      "date" : 1441908506000,
      "session" : "PRE",
      "participantDAO" : 5
   },
   {
      "id" : 2,
      "vivid" : 0,
   ...
```
**DELETE** *SERVER/api/export/NAME/ID*:  Removes a record from the Database.
For example: *curl -u admin@email.com:passwd  -X DELETE localhost:9000/api/export/ImageryPrime/1* would remove the item above.  This is secure delete, where the id linking the record to a participant is first overwritten, then deleted.




Testing
--------
$ ./gradlew test

Test results can be found in  ...PIServer/build/reports/tests/index.html

Security Overview
==================

Our Security Model is build on the popular Spring Security Framework.  Specifically version
3.2.3   We currently use a form based authentication (a web login form) that provides the following
basic projections and features:

* Every URL in the site requires authentication.
* CSRF attach prevention (http://en.wikipedia.org/wiki/Cross-site_request_forgery)
* Session Fixation Protection (http://en.wikipedia.org/wiki/Session_fixation)
* Security header integration
    * HTTP Strict Transport Security for secure requests
    * X-Content-Type-Options integration
    * Cache Control (can be overridden later by your application to allow caching of your static resources)
    * X-XSS-Protection integration
    * X-Frame-Options integration to help prevent Clickjacking

In production, we will secure the site using SSL encryption; all page requests occur over an HTTPS connection.



Adding a Questionnaire
======================

To Create a new Questionnaire you will to do 4 things:

1. Create an HTML web form to ask your questions.
2. Create a Java Model that represents the data from the form. (I promise this is simple)
3. A repository for storing your form data (Extremely simple)
4. Add details to the Questionnaire controller to allow you to correctly handle the form. 

The questionnaire must have a unique name from all other questionnaires.  It should not contain
spaces, or special characters, thought in a pinch it could use an underscore "_".  A good convention
is camelCase, where you upper case individual terms in your unique name, such as "UniqueName"

You will see references to **myForm** in the steps below.  Please replace this with the name of the form you are creating.  You may also see **MyForm** at which point you should upper case the first letter.  

Step 1:
-------
Create the html form.  New forms should be placed in 

/src/main/resources/templates/questions/**myForm**.html

It's a good idea to start with an existing form you like, and modify it.  However, there is nothing to prevent you from creating the page you want from scratch.  "Credibility" offers a good example of a simple one page form.  "Demographics" shows a multi-page form.  "DASS21" is a multi-page form with validation. 

Be sure to give the HTML <Form> tag a unique action.  
This will be used over again to wire your new questionnaire into the system, so make it unique and descriptive.  Making this the same as the file name of the form you are creating is recommended.
```
<form id="wizard" th:action="@{/questions/**myForm**}" method="POST">
```
From here, you just create your HTML form elements.  Give thoughtful names to these elements, you will be using them again in the next step.

You can see your form as you develop it.  Just execute:
```prompt
gradlew bootrun
```
and visit http:\\localhost:9000/questions/**myForm**

Any changes you make will be automatically visible by refreshing the page.  You don't need to stop and start the server to see your changes.

Step 2:
---------

Create a Java class for containing your form.  This should be located at:

/src/main/java/edu/virginia/psyc/pi/persistence/Questionnaire/**MyForm**.java
(please note the upper casing of the name)

This file defines how your data will be stored in the database.  While this looks an awful lot like programming, it is a very boilerplate format, that can be quickly implemented over and over again.

It should look roughly like this:

```java
package edu.virginia.psyc.pi.persistence.Questionnaire;

import lombok.Data;
import javax.persistence.*;

/**
 * The MyForm Web form.
 */
@Entity
@Table(name="MY_FORM")  // 1. The database is case insensitive, so using an _ can add a lot of clairty here.
@Data // 2. This will create a getX setX method for all our privately declared values.   
public class MyForm extends QuestionnaireData { // 3. Be sure to extend QuestionnaireData

   // 4. Define your form fields here using appropriate types.
   //    These should match exactly the "name" attribute on the
   //    the form elements created in Step 1.
   // -----------------------------------------------------
    private int someNumbericInput;
    private String anyTextualInput;
}

```

Step 3:
---------
Define a Java Repository - this file will be located here:
```
/src/main/java/edu/virginia/psyc/pi/persistence/Questionnaire/**MyForm**Repository.java
```
The Repository is VERY simple, and consists completely of only the content shown below.  It exists to give us a critical hook into the database if we need to access this data later in a unique way.

```java
package edu.virginia.psyc.pi.persistence.Questionnaire;

import edu.virginia.psyc.pi.persistence.ParticipantDAO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

 public interface **MyForm**Repository extends JpaRepository<**MyForm**, Long> {
     List<**MyForm**> findByParticipantDAO(ParticipantDAO p);				 
 }
				 
```

Step 4:
---------
Wire up the Controller

There is a Questionnaire controller located at:
```
/src/main/java/edu/virginia/psyc/pi/mvc/QuestionController.java
```
You aren't creating a new file this time, just adding a new method to an existing file.

You need to define an additional method on this controller, that will take the data from the form
covert it to our model in step 2, then use the repository in step 3 to store it in the Database. While this sounds complicated, the code is shown in full below. 

```java
    @Autowired private **MyForm**Repository **myForm**Repository;

    @RequestMapping(value = "**MyForm**", method = RequestMethod.GET)
    public ModelAndView show**MyForm**(Principal principal) {
        return modelAndView(principal, "/questions/**MyForm**", "**MyForm**", new **MyForm**());
    }

    @RequestMapping(value = "**MyForm**", method = RequestMethod.POST)
    RedirectView handle**MyForm**(@ModelAttribute("**MyForm**") **MyForm** **myForm**,
                                 BindingResult result, Principal principal) throws MessagingException {

        recordSessionProgress(**myForm**);
        **myForm**Repository.save(**myForm**);
        return new RedirectView("/session/next");
    }
```																 

That is it.  When participants fill out your new form, it will be stored in a new table named **MY_FORM** in the database.  From here we can create various reports to present this data which will be covered shortly.

