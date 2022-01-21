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

import java.io.IOException;
import java.util.Properties;

import com.compuware.jenkins.common.configuration.CpwrGlobalConfiguration;
import com.compuware.jenkins.common.configuration.HostConnection;
import com.google.common.base.Strings;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;

/**
 *
 */
public class TEDExecutionRunner implements IExecutionCommandArguments {
	
	private static final String TED_CLI_BAT = "TedCLI.bat";
	private static final String TED_CLI_SH = "TedCLI.sh";
	
	private final TEDExecutionBuilder tedBuilder;

	private Run<?, ?> build;
	private String remoteFileSeparator;

	/**
	 * Constructor
	 * 
	 * @param tedBuilder
	 * 			  An instance of <code>TEDExecutionBuilder</code> containing the arguments.
	 */
	public TEDExecutionRunner(TEDExecutionBuilder tedBuilder) {
		this.tedBuilder = tedBuilder;
		
	}
	
	/**
	 * @param build <code>Run</code>
	 * @param launcher <code>Launcher</code>
	 * @param workspaceFilePath  <code>FilePath</code>
	 * @param listener <code>TaskListener</code> 
	 * @return  <code>boolean</code>
	 * @throws IOException <code>IOException</code>IOException
	 * @throws InterruptedException <code>InterruptedException</code>
	 */
	public boolean run(final Run<?, ?> build, final Launcher launcher, final FilePath workspaceFilePath,
			final TaskListener listener) throws IOException, InterruptedException {
		// initialization
		ArgumentListBuilder args = new ArgumentListBuilder();
		EnvVars env = build.getEnvironment(listener);
		VirtualChannel vChannel = launcher.getChannel();
		
		if (vChannel == null){
			listener.getLogger().println("Error: No channel could be retrieved");
			return false;
		}
		
		this.build = build;
		Properties remoteProperties = vChannel.call(new RemoteSystemProperties());
		remoteFileSeparator = remoteProperties.getProperty("file.separator");
		String osScriptFile = launcher.isUnix() ? TED_CLI_SH : TED_CLI_BAT;

		TEDExecutionRunnerUtils.logJenkinsAndPluginVersion(listener);

		FilePath cliScriptPath = TEDExecutionRunnerUtils.getCLIScriptPath(launcher, listener, remoteFileSeparator, osScriptFile);
		args.add(cliScriptPath.getRemote());
		
		addArguments(args, launcher, listener, remoteFileSeparator);

		FilePath workDir = new FilePath (vChannel, workspaceFilePath.getRemote());
		workDir.mkdirs();

		listener.getLogger().println("----------------------------------");
		listener.getLogger().println("Now executing Enterprise Data Execution CLI and printing out the execution log...");
		listener.getLogger().println("----------------------------------\n\n");

		int exitValue = launcher.launch().cmds(args).envs(env).stdout(listener.getLogger()).pwd(workDir).join();

		listener.getLogger().println(osScriptFile + " exited with exit value = " + exitValue);
		listener.getLogger().println("\n\n----------------------------------");
		listener.getLogger().println("Enterprise Data Execution CLI finished executing, now analysing the result...");
		listener.getLogger().println("----------------------------------\n\n");

		if (exitValue != 0) {
			if (!tedBuilder.getHaltPipelineOnFailure()) {
				// Don't fail the build so the pipeline can continue.
				listener.getLogger().println("Test result failed but build continues (\"" + tedBuilder.getHaltPipelineTitle() + "\" is false)");
				exitValue = 0;
			} else {
				listener.getLogger().println("Specification Execution Failed.");
			}
		}

		return (exitValue == 0);
	}
	
	/**
	 * Adds an arguments to the argument list.
	 * 
	 * @param args
	 *		  The argument list to add to.
	 * @param launcher
	 *            The machine that the files will be checked out.
	 * @param listener
	 * 		  Build listener
	 * @param remoteFileSeparator
	 * 			  The remote file separator
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void addArguments(final ArgumentListBuilder args, final Launcher launcher, final TaskListener listener, final String remoteFileSeparator) throws IOException, InterruptedException {
		boolean min200903 = TEDExecutionRunnerUtils.isMinimumRelease(launcher, listener, remoteFileSeparator, TEDExecutionRunnerUtils.TTT_CLI_200903);
		
		if(!min200903) {
			return;
		}

		args.add(COMMAND[1]).add("execute");
		
		if(!Strings.isNullOrEmpty(tedBuilder.getRepositoryName())) {
			args.add(REPOSITORY[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getRepositoryName()));
		}
		if(!Strings.isNullOrEmpty(tedBuilder.getResultsRepositoryName())) {
			args.add(RESULTS_REPOSITORY[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getResultsRepositoryName()));
		}
		if(tedBuilder.isSingleSpecExecution()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getSpecificationName())) {
				args.add(SPECIFICATION[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationName()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getSpecificationType())) {
				args.add(SPECIFICATION_TYPE[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationType()));
			}
		} else {
			if(!Strings.isNullOrEmpty(tedBuilder.getSpecificationList())) {
				args.add(SPECIFICATION_LIST[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationList()));
			}
			args.add(EXIT_ON_FAILURE[1]).add(tedBuilder.getExitOnFailure()?"true":"false");
		}
		if(!Strings.isNullOrEmpty(tedBuilder.getExecutionTimeout())) {
			args.add(EXECUTION_TIMEOUT[1]).add(tedBuilder.getExecutionTimeout());
		}
		if(!Strings.isNullOrEmpty(tedBuilder.getExecutionContext())) {
			String exContext = tedBuilder.getExecutionContext();
			
			if(!exContext.contains("/") && !exContext.contains("\\")) {
				StringBuilder sb = new StringBuilder(TEDExecutionRunnerUtils.getTopaWorkbenchCLIPath(launcher));
				sb.append(remoteFileSeparator).append("EnterpriseData").append(remoteFileSeparator).append(exContext);
				exContext =  sb.toString(); 
			}
			
			args.add(EXECUTION_CONTEXT[1]).add(TEDExecutionRunnerUtils.escapeForScript(exContext));
		}
		
		addCESArguments(args);		
		addCommunicationManagerArguments(args);
		addExecutionServerArguments(args);
		addMainframeSpecificArguments(args);
	}

	/**
	 * @param args
	 */
	private void addCESArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineCES()) {
			args.add(USE_CLOUD_CES[1]).add(tedBuilder.getUseCloudCES()?"true":"false");

			if(!Strings.isNullOrEmpty(tedBuilder.getCesURL())) {
				args.add(CES_URL[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getCesURL()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getCloudCustomerNo())) {
				args.add(CES_CUSTOMER_NUMBER[1]).add(tedBuilder.getCloudCustomerNo());
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getCloudSiteID())) {
				args.add(CES_SITE_ID[1]).add(tedBuilder.getCloudSiteID());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addCommunicationManagerArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineManager()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getCommunicationManager())) {
				args.add(COMM_MANAGER[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getCommunicationManager()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getCommunicationManagerPort())) {
				args.add(COMM_MANAGER_PORT[1]).add(tedBuilder.getCommunicationManagerPort());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addExecutionServerArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineServer()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getExecutionServer())) {
				args.add(EXECUTION_SERVER[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getExecutionServer()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getExecutionServerPort())) {
				args.add(EXECUTION_SERVER_PORT[1]).add(tedBuilder.getExecutionServerPort());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addMainframeSpecificArguments(final ArgumentListBuilder args) {
		addHostConnectionInfoArguments(args);
		addHostCredentialsArguments(args);
		addJCLJobcardArguments(args);
		addDatasetQualifierArguments(args);
		addDataPrivacyOverrideArguments(args);
	}

	/**
	 * @param args
	 */
	private void addHostConnectionInfoArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineHost()  && !Strings.isNullOrEmpty(tedBuilder.getConnectionId())) {
			HostConnection connection = null;
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
	
			if (globalConfig != null){
				connection = globalConfig.getHostConnection(tedBuilder.getConnectionId());
			}
			if (connection != null) {
				args.add(EXECUTION_HOST[1]).add(connection.getHost());
				args.add(EXECUTION_HOST_PORT[1]).add(connection.getPort());
				args.add(CCSID[1]).add(connection.getCodePage());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addHostCredentialsArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getIncludeCred()) {
			String hostCreds = tedBuilder.getCredentialsId();
			if(!Strings.isNullOrEmpty(hostCreds)) {
				args.add(HCI_USER_ID[1]).add(TEDExecutionRunnerUtils.getLoginInformation(build.getParent(), hostCreds).getUsername(), false);
				args.add(HCI_PASSWORD[1]).add(TEDExecutionRunnerUtils.getLoginInformation(build.getParent(), hostCreds).getPassword(), true);
			}
		}
	}

	/**
	 * @param args
	 */
	private void addJCLJobcardArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineJobcard()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine1())) {
				args.add(JCL_JOBCARD1[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine1()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine2())) {
				args.add(JCL_JOBCARD2[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine2()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine3())) {
				args.add(JCL_JOBCARD3[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine3()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine4())) {
				args.add(JCL_JOBCARD4[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine4()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine5())) {
				args.add(JCL_JOBCARD5[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine5()));
			}
		}
	}

	/**
	 * @param args
	 */
	private void addDatasetQualifierArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineQualifiers()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getDatasetHighLevelQualifier())) {
				args.add(DATASET_HLQ[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDatasetHighLevelQualifier()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getTemporaryDatasetPrefix())) {
				args.add(TEMP_DATASET_PREFIX[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getTemporaryDatasetPrefix()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getTemporaryDatasetSuffix())) {
				args.add(TEMP_DATASET_SUFFIX[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getTemporaryDatasetSuffix()));
			}
		}
	}

	/**
	 * @param args
	 */
	private void addDataPrivacyOverrideArguments(final ArgumentListBuilder args) {
		if(tedBuilder.getDefineDataprivacyOverride()) {
			if(!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFADEBUG())) {
				args.add(FADEBUG[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFADEBUG()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAEXPATH())) {
				args.add(FAEXPATH[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAEXPATH()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAIPADDR())) {
				args.add(FAIPADDR[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAIPADDR()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAJOPTS())) {
				args.add(FAJOPTS[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAJOPTS()));
			}
			if(!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAJPATH())) {
				args.add(FAJPATH[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAJPATH()));
			}
		}
	}
	
}
