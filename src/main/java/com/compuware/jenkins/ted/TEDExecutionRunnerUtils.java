/**
 * The MIT License (MIT)
 * 
 * (c) Copyright 2022 BMC Software, Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.compuware.jenkins.ted;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.matchers.IdMatcher;
import com.compuware.jenkins.common.configuration.CpwrGlobalConfiguration;
import com.compuware.jenkins.common.utils.CLIVersionUtils;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Plugin;
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.security.ACL;
import jenkins.model.Jenkins;

public class TEDExecutionRunnerUtils {

	public static final String TTT_CLI_200903 = "20.09.03";
	public static final String TED_MINIMUM_CLI_VERSION = TTT_CLI_200903; //Currently no changes after 20903

	private static final String DOUBLE_QUOTE = "\"";
	private static final String DOUBLE_QUOTE_ESCAPED = "\"\"";

	/**
	 * Retrieves login information given a credential ID
	 * 
	 * @param project       the Jenkins project
	 * @param credentialsId The credendtial id for the user.
	 * 
	 * @return a Jenkins credential with login information
	 */
	public static StandardUsernamePasswordCredentials getLoginInformation(Item project, String credentialsId) {
		StandardUsernamePasswordCredentials credential = null;

		List<StandardUsernamePasswordCredentials> credentials = CredentialsProvider.lookupCredentials(
				StandardUsernamePasswordCredentials.class, project, ACL.SYSTEM,Collections.<DomainRequirement>emptyList());

		IdMatcher matcher = new IdMatcher(credentialsId);
		for (StandardUsernamePasswordCredentials cred : credentials) {
			if (matcher.matches(cred)) {
				credential = cred;
			}
		}

		return credential;
	}

	/**
	 * Returns the path to the script to execute TED CLI
	 * 
	 * @param launcher      An instance <code>Launcher</code> for launching the
	 *                      script.
	 * @param listener      An instance of <code>TaskListener</code> for the task.
	 * @param fileSeparator The file separator for the system on which the script
	 *                      will run.
	 * @param osScriptFile  The name of the operating system dependent script file
	 *                      to run.
	 * 
	 * @return An instance of <code>FilePath</code> for the CLI directory
	 * 
	 * @throws IOException          If the CLI directory does not exist.
	 * @throws InterruptedException If unable to get CLI directory.
	 */
	public static FilePath getCLIScriptPath(final Launcher launcher, final TaskListener listener,
			final String fileSeparator, final String osScriptFile) throws IOException, InterruptedException {
		return getCLIScriptPath(launcher, listener, fileSeparator, osScriptFile, TED_MINIMUM_CLI_VERSION);
	}

	/**
	 * Returns the path to the script to execute TED CLI
	 * 
	 * @param launcher      An instance <code>Launcher</code> for launching the
	 *                      script.
	 * @param listener      An instance of <code>TaskListener</code> for the task.
	 * @param fileSeparator The file separator for the system on which the script
	 *                      will run.
	 * @param osScriptFile  The name of the operating system dependent script file
	 *                      to run.
	 * @param minCLIRelease The minimum CLI release required to run the script.
	 * 
	 * @return An instance of <code>FilePath</code> for the CLI directory
	 * 
	 * @throws IOException          If the CLI directory does not exist.
	 * @throws InterruptedException If unable to get CLI directory.
	 */
	public static FilePath getCLIScriptPath(final Launcher launcher, final TaskListener listener,
			final String fileSeparator, final String osScriptFile, final String minCLIRelease)
			throws IOException, InterruptedException {
		FilePath topazWorkbenchCLIPath = null;
		FilePath cliScriptPath = null;

		VirtualChannel vChannel = launcher.getChannel();

		String cliDirectoryName = getTopaWorkbenchCLIPath(launcher);
		if (cliDirectoryName != null) {
			topazWorkbenchCLIPath = new FilePath(vChannel, cliDirectoryName);
		}

		if (topazWorkbenchCLIPath == null) {
			throw new FileNotFoundException(
					"ERROR: Topaz Workench CLI location was not specified. Check 'Compuware Configuration' section under 'Configure System'"); //$NON-NLS-1$
		} else {
			if (!topazWorkbenchCLIPath.exists()) {
				throw new FileNotFoundException("ERROR: Topaz Workench CLI location does not exist. Location: " //$NON-NLS-1$
						+ topazWorkbenchCLIPath.getRemote()
						+ ". Check 'Compuware Configuration' section under 'Configure System'"); // NOSONAR //$NON-NLS-1$
			}

			String cliScriptFile = topazWorkbenchCLIPath.getRemote() + fileSeparator + osScriptFile;
			cliScriptPath = new FilePath(vChannel, cliScriptFile);
			listener.getLogger().println("Topaz for Enterprise Data CLI script path: " + cliScriptPath.getRemote()); //$NON-NLS-1$

			String cliVersion = getCLIVersion(launcher, fileSeparator);
			CLIVersionUtils.checkCLICompatibility(cliVersion, minCLIRelease);
		}

		return cliScriptPath;
	}

	/**
	 * Returns the path of the Topaz Workbench CLI, as defined in the global
	 * Jenkins' System settings.
	 * 
	 * @param launcher An instance <code>Launcher</code> for launching the script.
	 * 
	 * @return The path to The Topaz Workbench CLI
	 */
	public static String getTopaWorkbenchCLIPath(final Launcher launcher) {
		String cliDirectoryName = null;
		CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
		if (globalConfig != null) {
			cliDirectoryName = globalConfig.getTopazCLILocation(launcher);
		}

		return cliDirectoryName;
	}

	/**
	 * Returns the version of the TED CLI
	 * 
	 * @param launcher            The machine that the files will be checked out.
	 * @param remoteFileSeparator The remote file separator
	 * 
	 * @return An instance of <code>FilePath</code> for the CLI directory
	 * 
	 * @throws IOException          If the CLI directory does not exist.
	 * @throws InterruptedException If unable to get CLI directory.
	 */
	public static String getCLIVersion(final Launcher launcher, String remoteFileSeparator) throws IOException, InterruptedException {
		String cliVersion = null;
		FilePath globalCLIDirectory = null;
		CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
		
		if (globalConfig != null) {
			String cliDirectoryName = globalConfig.getTopazCLILocation(launcher);
			if (cliDirectoryName != null) {
				VirtualChannel vChannel = launcher.getChannel();
				globalCLIDirectory = new FilePath(vChannel, cliDirectoryName);
			}
		}

		if (globalCLIDirectory == null) {
			throw new FileNotFoundException(
					"ERROR: Topaz Workench CLI location was not specified. Check 'Compuware Configuration' section under 'Configure System'"); //$NON-NLS-1$
		} else {
			if (!globalCLIDirectory.exists()) {
				throw new FileNotFoundException(
						"ERROR: Topaz Workench CLI location does not exist. Location: " + globalCLIDirectory.getRemote() //$NON-NLS-1$
								+ ". Check 'Compuware Configuration' section under 'Configure System'"); // NOSONAR //$NON-NLS-1$
			} else {
				cliVersion = CLIVersionUtils.getCLIVersion(globalCLIDirectory, TED_MINIMUM_CLI_VERSION);
			}
		}

		return cliVersion;
	}

	/**
	 * Logs the Jenkins and TED Plugin versions
	 * 
	 * @param listener An instance of <code>TaskListener</code> for the task.
	 */
	public static void logJenkinsAndPluginVersion(final TaskListener listener) {
		listener.getLogger().println("Jenkins Version: " + Jenkins.VERSION); //$NON-NLS-1$
		Jenkins jenkinsInstance = Jenkins.getInstanceOrNull();
		if (jenkinsInstance != null) {
			Plugin pluginV1 = jenkinsInstance.getPlugin("compuware-topaz-for-enterprise-data"); //$NON-NLS-1$
			if (pluginV1 != null) {
				listener.getLogger().println("Topaz for Enterprise Data Jenkins Plugin: " //$NON-NLS-1$
						+ pluginV1.getWrapper().getShortName() + " Version: " + pluginV1.getWrapper().getVersion()); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Returns if the TED CLI is greater or equal to the passed version.
	 * 
	 * @param launcher
	 *            The machine that the files will be checked out.
	 * @param listener
	 * 			An instance of <code>TaskListener</code> for the task.
	 * @param remoteFileSeparator
	 * 			  The remote file separator
	 * @param versionNumber
	 * 			  The minimum TED CLI version number to check against.
	 *            
	 * @return	<code>true</code> if this node supports using the local configuration directory, otherwise <code>false</code>.
	 */
	public static boolean isMinimumRelease (final Launcher launcher, final TaskListener listener, String remoteFileSeparator, final String versionNumber) {
		boolean isminimumRelease = true;
		
		try {
			String cliVersion = getCLIVersion(launcher, remoteFileSeparator);
			
			try {
				CLIVersionUtils.checkCLICompatibility(cliVersion, versionNumber);
			} catch (Exception e) {
				isminimumRelease = false;
			}
		} catch (Exception e) {
			isminimumRelease = false;
		}

		return isminimumRelease;
	}
	
	/**
	 * Returns an escaped version of the given input String for a Batch or Shell script.
	 * 
	 * @param input
	 *            the <code>String</code> to escape
	 * 
	 * @return the escaped <code>String</code>
	 */
	public static String escapeForScript(final String input) {
		String output = null;

		if (input != null) {
			// escape any double quotes (") with another double quote (") for both batch and shell scripts
			output = StringUtils.replace(input, DOUBLE_QUOTE, DOUBLE_QUOTE_ESCAPED);
		}

		return output;
	}
}
