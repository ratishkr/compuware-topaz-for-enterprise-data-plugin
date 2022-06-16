### Overview

Compuware's Topaz for Enterprise Data is a data management product with automation to speed data extraction, privatization and loading. The plugin allows Jenkins users to run ted specifications.

### Change Log

To access the change log, go to
[Topaz for Enterprise Data Change log](https://github.com/jenkinsci/compuware-topaz-for-enterprise-data-plugin/blob/master/CHANGELOG.md)

### Prerequisites

The following are required to use this plugin:

-   Jenkins
-   Jenkins Credentials Plugin
-   Topaz Workbench CLI.Refer to the [Topaz Workbench Install
    Guide](http://frontline.compuware.com/Doc/KB/KB1802/PDF/Topaz_Workbench_Install_Guide.pdf) for
    instructions.
-   Topaz for Enterprise Data license.
-   Host Communications Interface

### Installing in a Jenkins Instance

1.  Install the Compuware Topaz for Enterprise Data plugin according to the Jenkins instructions for installing plugins. Dependent plugins will automatically be installed.
2.  Install the Topaz Workbench CLI on the Jenkins instances that will execute the plugin. The Topaz Workbench CLI is available on the Topaz Workbench installation package. If you do not have the installation package, please  visit [go.compuware.com](http://go.compuware.com/). For Topaz Workbench CLI installation instructions, please refer to the[Topaz Workbench Install Guide](http://frontline.compuware.com/Doc/KB/KB1802/PDF/Topaz_Workbench_Install_Guide.pdf)

### Configuring Host Connections
	In order to use Topaz for Enterprise Data, you will need to point to an installed Topaz Workbench Command Line Interface (CLI). The Topaz Workbench CLI will work with host connection(s) you also need to configure Topaz for Enterprise Data members.
    - See [Configuring for Topaz Workbench CLI & Host Connections](https://github.com/jenkinsci/compuware-common-configuration-plugin/blob/master/README.md#configuring-for-topaz-workbench-cli--host-connections)
    
### Executing Unit tests

1.  Install the Compuware Topaz for Enterprise Data plugin according to the Jenkins instructions for installing plugins.

2.  In the Jenkins system configuration page's **Topaz Workbench CLI**, point to the Windows and/or Linux installation location(s) of the CLI. If necessary, change the default values given to match the correct installation location(s).

    **Note**: The Topaz Workbench CLI must be installed on the machine that is configured to run the job.

3.  On the project Configuration page, in the **Build** section click **Add build step** button and select **Topaz for Enterprise Data**.

4.  Enter the name of repository, and the results repository if applicable. 

5.  Choose to either execute a single specification or multiple and provide the specification info under the appropriate section. 

6.  Provide the name or the path of execution context (if applicable) on the machine where the Topaz CLI is installed.       

7.  Provide the communication manager, execution server and execution host info in the appropriate sections. All of this can be provide via the execution context. 

8.  Under the execution host section, in the **Login credentials**, select the stored credentials to use for logging onto the host. Alternatively, click **Add** add
    credentials using the Credentials Plugin. Refer to the Jenkins documentation for the Credentials Plugin.
    
9.  If you would like use a keystore or certificate value to authenticate into the execution host, define them in the execution context. Do not select an user id/password in that case.     

10. Choose the 'Halt pipeline if errors occur' checkbox, if you would want the parent jenkins pipeline to terminate if the specification execution encounters a failure. 

11. Enter JCL jobcard , dataset and dataprivacy overrides if required for executing RDX specifications. 

12. Click **Save**.

# Product Assistance

Compuware provides assistance for customers with its documentation, the Compuware Support Center web site, and telephone customer support.

## Compuware Support Center

You can access online information for Compuware products via our Support Center site at [https://go.compuware.com](https://go.compuware.com/) Support Center provides access to critical information about your Compuware products. You can review frequently asked questions, read or download documentation, access product fixes, or e-mail your questions or comments. The first time you access Support Center, you must register and obtain a password. Registration is free.

Compuware also offers User Communities, online forums to collaborate, network, and exchange best practices with other Compuware solution users worldwide. Go to <http://groups.compuware.com/> to join.

## Contacting Customer Support

At Compuware, we strive to make our products and documentation the best in the industry. Feedback from our customers helps us maintain our quality standards. If you need support services, please obtain the following information before calling Compuware's 24-hour telephone support:

-   The name, release number, and build number of your product. This information is displayed in the **About** dialog box.

-   Installation information including installed options, whether the product uses local or network databases, whether it is installed in the default directories, whether it is a standalone or network installation, and whether it is a client or server installation.

-   Environment information, such as the operating system and release on which the product is installed, memory, hardware and network specification, and the names and releases of other applications that were running when the problem occurred.

-   The location of the problem within the running application and the user actions taken before the problem occurred.

-   The exact application, licensing, or operating system error messages, if any.

You can contact Compuware in one of the following ways:

### Phone

-   USA and Canada: 1-800-538-7822 or 1-313-227-5444.

-   All other countries: Contact your local Compuware office. Contact information is available at [https://go.compuware.com](https://go.compuware.com/)

### Web

You can report issues via Compuware Support Center.

**Note:** Please report all high-priority issues by phone.

### Mail

Customer Support  
Compuware Corporation  
One Campus Martius  
Detroit, MI 48226-5099

## Corporate Web Site

To access Compuware's site on the Web, go to [https://www.compuware.com](https://www.compuware.com/). The Compuware site provides a variety of product and support information.
