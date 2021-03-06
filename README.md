FIT Center Welcome Console
===========================

The ITS FIT (Faculty Instructional Technology) Center is a great place for San Diego State University Faculty to learn and use Learning Technologies, like OMR machines and classroom response tools.

Tracking why Faculty come to the FIT Center can help to improve the services and support they receive. This is where the FIT Welcome Console can help. Running a web-enabled device, like a desktop or tablet, guests can check in, providing statical tracking, as well as notifications in certain cases.

We also use [Acuity Scheduling](https://acuityscheduling.com/) to manage ParScore appointments. FIT Welcome integrates with Acuity to streamline the check-in process for faculty who have already made appointments. Faculty can also schedule appointments directly from within the Welcome Console.

FIT Welcome also includes various staff tools, including...
- Clock In/Out capabilities for Hourly Employees
- Reporting Tools for User and Staff Activity
- Live Activity - Visit /live and enter a staff ID to view.


## Setup
FIT Welcome is written primarily in Java and is run using the TomCat framework to run the WebSites. A MySQL DB is used to store user information, as well as log events and clock in/out times.

__FollowUp__ is an integrated module that will prompt recent visitors to provide feedback on the service that they received with a given schedule, like at the end of every day (or whenever you like) that sends a follow up email with a survey link to users who have visited within the last X days. There is also a param to limit the number of emails a user gets.
 - Param: `followup_enable` - Enables the Job that check for eligible events and send emails to the respective parties
 - Param: `followup_schedule` - A CRON String that determines the schedule that Follow Up messages will be sent.
 - Param: `followup_freshness` - Within how many days to contact the users. For example a value of 3 would mean to contact those who have visited in the last 3 days.
 - Param: `followup_max` - The minimum number of days since the user was last emailed. For Example a value of 7 would mean that a user who visits every day would only get an email every 7 days.

__Alerts__ is an integrated module that will send an alert to all users who are still clocked-in at the time that the job is run. This is intedned to be run at the end of the business day to alert cockable staff who forgot to clock out – as staff who are not clocked-out will not be able to clock-in the next day.
 - Param: `alerts_enable` - Enables the Job that reminds clocked-in users of their status
 - Param: `alerts_schedule` - A CRON String that determines the schedule that Alert messages will be sent.

__Blackboard__ can be integrated to sync Users who have a Numerical Student ID, external ID, or Username with your Users Table in the DB. 

For generating CRON Schedules for Quartz Scheduler, the Job Agent that has been implemented, https://www.freeformatter.com/cron-expression-generator-quartz.html may come in very useful, since Quartz implements a unix-like format, but it is slightly different and strings are not cross compatible between `crontab` and Quartz.

 - ___Full Param List is below!___


FIT Welcome also uses [Vault](https://vaultproject.io) to access credentials for various tools and services (DataBase, APIs, Email, etc.)

### Locales
Locales allow for different kiosks to show different options based on the URL parameter, `locale`. The default local can be set by modifying `renderMap.js` in `webapps/js`, and changing the `defaultLocale` constant at the top of the file. The default is set to FIT by default.

The locales folder in `webapps` houses all of the sitemaps, as well as assets for every locale. The name of the folder is the name of the locale that will be used by the Web Interface. Each folder should contain `apple-touch-icon.png`, `logo.png`, and `sitemap.json`.

### DB Config
To setup the various tables that FIT Welcome uses, run the code below in the Database that will be used with FIT Welcome.

#### Table Breakdown
- __users__ is where all Primary Users (Faculty, TAs, etc.) should be stored.
- __clock__ is used to store Clock In/Out events.
- __events__ stores all check-in information, as well as any additional information that is relevant to that check-in.
- __quotes__ stores a list of quotes that are displayed on the confirmation pages. These quotes can be anything you like! (A good source for quotes is [Brainy Quote](http://www.brainyquote.com/)).
- __staff__ stores information for all staff users.
- __meeting__ saves the values that should be saved in the event log for each Acuity Appointment Type. These values are set in the Admin Panel.
- __emails__ is used by the FollowUp module to track who was sent emails when.

__Important Note:__ information in the __staff__ table has priority over information in the __users__ table; this is done to allow normal users to be changed to staff users without the need to remove them from the primary users table.

#### Both
Run the below commands in both your production and testing/staging databases.
``` sql

CREATE TABLE dsk (
  `PK`   INT AUTO_INCREMENT PRIMARY KEY,
  `name` TEXT
);

INSERT INTO dsk (`name`) VALUES ('EXTERNAL');

CREATE TABLE department (
  `PK`   INT AUTO_INCREMENT PRIMARY KEY,
  `name` TEXT
);

CREATE TABLE users
(
  id          INT(9)                              NOT NULL
    PRIMARY KEY,
  first_name  TEXT                                NULL,
  last_name   TEXT                                NULL,
  email       TEXT                                NULL,
  send_emails TINYINT(1) DEFAULT '1'              NULL,
  dsk         INT                                 NULL,
  department  INT                                 NULL,
  updated     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  FOREIGN KEY (dsk) REFERENCES dsk (PK),
  FOREIGN KEY (department) REFERENCES department (PK)
);

CREATE TABLE clock
(
    id INT(9),
    time_in TIMESTAMP,
    time_out TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL
);
CREATE TABLE events
(
    ID int(9) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    TIMESTAMP TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL,
    redid INT(9),
    action TEXT,
    params TEXT,
    FOREIGN KEY (redid) REFERENCES users (id)
);
CREATE TABLE quotes
(
    id INT(9) PRIMARY KEY NOT NULL,
    text TEXT,
    author TEXT
);
CREATE TABLE staff
(
    id INT(9) PRIMARY KEY NOT NULL,
    first_name TEXT,
    last_name TEXT,
    email TEXT,
    clockable TINYINT(1),
    admin TINYINT(1),
    instructional_designer TINYINT(1)
);
CREATE TABLE meetings (
  `acuity_id`    INT PRIMARY KEY NOT NULL,
  `event_text`   TEXT,
  `event_params` TEXT
);
CREATE TABLE email
(
    TIMESTAMP TIMESTAMP DEFAULT '0000-00-00 00:00:00' NOT NULL,
    ID int(9),
    TYPE TEXT
);
INSERT INTO users VALUES (999999999, 'WalkIn', 'User', 'nobody@blackboard.sdsu.edu', 0);
```

#### Testing/Staging
Run the set of commands below in your testing/staging database __in addition to__ the commands listed above.
``` sql
INSERT INTO users VALUES (100000001, 'Test', 'User', 'unique_email@blackboard.sdsu.edu', 1);
INSERT INTO staff VALUES (123456789, 'Test', 'Staff', 'staff@blackboard.sdsu.edu', 1, 0, 0);
INSERT INTO staff VALUES (123123123, 'Test', 'Admin', 'admin@blackboard.sdsu.edu', 0, 1, 0);
```

### Blackboard Setup
In order to interface with Blackboard and it's REST API, you will need to be running Blackboard LEARN version 3000 or higher.
Additionally, you will need to regsiter with the Blackboard Developer site, and create an application in order to obtain an
Application ID, Application Key, Application Secret. Make note of these, especially the Secret, as they are only shown once
and you will need to save them in the Vault (described below).
Once you have your application ID, you will need to authorize the application with Blackboard. You can do this by going into
the Blackboard Administrator Panel, then going into "REST API Integrations" and Creating an Integration. Enter the Application
ID you obtained earlier and you should be good to go.

### Vault Setup
You will need to create two secrets in the vault, one that will have the information for your production system, the
other with your testing configuration. Information on how to setup Vault and AppRoles can be found at: https://sdsu-its.gitbooks.io/vault/content/

The name of the app that you want to use needs to be set as the `WELCOME_APP` environment variable.
You will also need to set the `VAULT_ADDR`, `VAULT_ROLE` and `VAULT_SECRET` environment variables to their corresponding values.

#### Production
- `alert_enable` = Enables Non-Clock Out Email Alert Messages
- `alert_schedule` = CRON Schedule for Non-Clock Out emails
- `bb-API-secret` = Name of the Secret with the Blackboard Application Credentials
- `bb-url` = Blackboard URL
- `syncEnable` = Enable/Disable (T/F) Blackboard Sync
- `syncFrequency` = How often the Sync Job should run (30s recommended)
- `db-password` = Database Password
- `db-url` = jdbc:mysql://db_host:3306/db_name _replace db_host, db_name and possibly the port with your MySQL server info_
- `db-user` = Database Username
- `followup_enable` = Enables Follow Up Survey Emails
- `followup_schedule` = CRON Schedule for Follow Up Survey Emails to be sent 
- `followup_freshness` = The maximum amount of time since the user's last visit to email the user
- `followup_max` = The minimum number of days between emails
- `followup_survey_link` = Survey Link for the User, use the string `{{ event_id }}` to fill in the Event ID.
Example: [http://www.bing.com/images/search?q={{ event_id }}](http://www.bing.com/images/search?q={{ event_id }})
- `followup_unsubscribe` = Unsubscribe Link, use `{{ email }}` to substitute the email.
Example:  [http://your_domian_with_context/followup/unsubscribe.html?email={{ email }}](http://your_domian_with_context/followup/unsubscribe.html?email={{ email }})

#### Testing/Staging
Make sure that at least the `db_name` is different than your production settings.
- `db-password` = Database Password
- `db-url` = jdbc:mysql://db_host:3306/db_name _replace db_host, db_name and possibly the port with your MySQL server info_
-	`db-user` = Database Username

#### Email (`fit_email`)
Create an additional application with the email credentials.
- `host`
- `post`
- `username`
- `password`
- `from_email`
- `from_name`

#### Acuity (`acuity`)
Finally, create a fourth application with the Acuity Scheduling configuration information.
- `User ID`
- `API Key`

#### Blackboard (`bb-api-user-sync`)
You will need to create a Blackboard Application, with the Blackboard Developer Site, as described above.
Save the various values that you got during the application creation process as the following values in the Vault 
respectively. 
- `applicationID`
- `key`
- `secret`