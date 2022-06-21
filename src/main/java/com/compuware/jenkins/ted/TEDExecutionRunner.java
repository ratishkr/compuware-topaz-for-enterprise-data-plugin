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
public class TEDExecutionRunner {

	private static final String TED_CLI_BAT = "TedCLI.bat";
	private static final String TED_CLI_SH = "TedCLI.sh";

	private final TEDExecutionBuilder tedBuilder;

	private Run<?, ?> build;

	/**
	 * Constructor
	 * 
	 * @param tedBuilder
	 *            An instance of <code>TEDExecutionBuilder</code> containing the arguments.
	 */
	public TEDExecutionRunner(TEDExecutionBuilder tedBuilder) {
		this.tedBuilder = tedBuilder;

	}

	/**
	 * @param build
	 *            <code>Run</code>
	 * @param launcher
	 *            <code>Launcher</code>
	 * @param workspaceFilePath
	 *            <code>FilePath</code>
	 * @param listener
	 *            <code>TaskListener</code>
	 * @return <code>boolean</code>
	 * @throws IOException
	 *             <code>IOException</code>IOException
	 * @throws InterruptedException
	 *             <code>InterruptedException</code>
	 */
	public boolean run(final Run<?, ?> build, final Launcher launcher, final FilePath workspaceFilePath, final TaskListener listener)
			throws IOException, InterruptedException {
		// initialization
		ArgumentListBuilder args = new ArgumentListBuilder();
		EnvVars env = build.getEnvironment(listener);
		VirtualChannel vChannel = launcher.getChannel();

		if (vChannel == null) {
			listener.getLogger().println("Error: No channel could be retrieved");
			return false;
		}

		this.build = build;
		Properties remoteProperties = vChannel.call(new RemoteSystemProperties());
		String remoteFileSeparator = remoteProperties.getProperty("file.separator");
		String osScriptFile = launcher.isUnix() ? TED_CLI_SH : TED_CLI_BAT;

		TEDExecutionRunnerUtils.logJenkinsAndPluginVersion(listener);

		FilePath cliScriptPath = TEDExecutionRunnerUtils.getCLIScriptPath(launcher, listener, remoteFileSeparator, osScriptFile);
		args.add(cliScriptPath.getRemote());

		addArguments(args, launcher, listener, remoteFileSeparator);

		FilePath workDir = new FilePath(vChannel, workspaceFilePath.getRemote());
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
				listener.getLogger()
						.println("Test result failed but build continues (\"" + tedBuilder.getHaltPipelineTitle() + "\" is false)");
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
	 *            The argument list to add to.
	 * @param launcher
	 *            The machine that the files will be checked out.
	 * @param listener
	 *            Build listener
	 * @param remoteFileSeparator
	 *            The remote file separator
	 */
	private void addArguments(final ArgumentListBuilder args, final Launcher launcher, final TaskListener listener,
			final String remoteFileSeparator) {
		boolean min200903 = TEDExecutionRunnerUtils.isMinimumRelease(launcher, listener, remoteFileSeparator,
				TEDExecutionRunnerUtils.TTT_CLI_200903);

		if (!min200903) {
			return;
		}

		args.add(ExecutionCommandArguments.getCOMMAND()[1]).add("execute");

		addRepositoryArguments(args);
		addSpecificationArguments(args);

		if (!Strings.isNullOrEmpty(tedBuilder.getExecutionTimeout())) {
			args.add(ExecutionCommandArguments.getExecutionTimeout()[1]).add(tedBuilder.getExecutionTimeout());
		}
		if (!Strings.isNullOrEmpty(tedBuilder.getExecutionContext())) {
			String exContext = tedBuilder.getExecutionContext();

			if (!exContext.contains("/") && !exContext.contains("\\")) {
				StringBuilder sb = new StringBuilder(TEDExecutionRunnerUtils.getTopaWorkbenchCLIPath(launcher));
				sb.append(remoteFileSeparator).append("EnterpriseData").append(remoteFileSeparator).append(exContext);
				exContext = sb.toString();
			}

			args.add(ExecutionCommandArguments.getExecutionContext()[1]).add(TEDExecutionRunnerUtils.escapeForScript(exContext));
		}

		addCESArguments(args);
		addCommunicationManagerArguments(args);
		addExecutionServerArguments(args);
		addMainframeSpecificArguments(args);
	}

	/**
	 * Adds arguments related to repository.
	 * 
	 * @param args
	 *            - list of arguments.
	 */
	private void addRepositoryArguments(final ArgumentListBuilder args) {
		if (!Strings.isNullOrEmpty(tedBuilder.getRepositoryName())) {
			args.add(ExecutionCommandArguments.getREPOSITORY()[1])
					.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getRepositoryName()));
		}
		if (!Strings.isNullOrEmpty(tedBuilder.getResultsRepositoryName())) {
			args.add(ExecutionCommandArguments.getResultsRepository()[1])
					.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getResultsRepositoryName()));
		}
	}

	/**
	 * Adds arguments related to specification.
	 * 
	 * @param args
	 *            - list of arguments.
	 */
	private void addSpecificationArguments(final ArgumentListBuilder args) {
		if (tedBuilder.isSingleSpecExecution()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getSpecificationName())) {
				args.add(ExecutionCommandArguments.getSPECIFICATION()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationName()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getSpecificationType())) {
				args.add(ExecutionCommandArguments.getSpecificationType()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationType()));
			}
		} else {
			if (!Strings.isNullOrEmpty(tedBuilder.getSpecificationList())) {
				args.add(ExecutionCommandArguments.getSpecificationList()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getSpecificationList()));
			}
			args.add(ExecutionCommandArguments.getExitOnFailure()[1]).add(tedBuilder.getExitOnFailure() ? "true" : "false");
		}
	}

	/**
	 * @param args
	 */
	private void addCESArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineCES()) {
			args.add(ExecutionCommandArguments.getUseCloudCES()[1]).add(tedBuilder.getUseCloudCES() ? "true" : "false");

			if (!Strings.isNullOrEmpty(tedBuilder.getCesURL())) {
				args.add(ExecutionCommandArguments.getCESURL()[1]).add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getCesURL()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getCloudCustomerNo())) {
				args.add(ExecutionCommandArguments.getCESCustomerNumber()[1]).add(tedBuilder.getCloudCustomerNo());
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getCloudSiteID())) {
				args.add(ExecutionCommandArguments.getCESSiteID()[1]).add(tedBuilder.getCloudSiteID());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addCommunicationManagerArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineManager()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getCommunicationManager())) {
				args.add(ExecutionCommandArguments.getCommManager()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getCommunicationManager()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getCommunicationManagerPort())) {
				args.add(ExecutionCommandArguments.getCommManagerPort()[1]).add(tedBuilder.getCommunicationManagerPort());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addExecutionServerArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineServer()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getExecutionServer())) {
				args.add(ExecutionCommandArguments.getExecutionServer()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getExecutionServer()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getExecutionServerPort())) {
				args.add(ExecutionCommandArguments.getExecutionServerPort()[1]).add(tedBuilder.getExecutionServerPort());
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
		if (tedBuilder.getDefineHost() && !Strings.isNullOrEmpty(tedBuilder.getConnectionId())) {
			HostConnection connection = null;
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();

			if (globalConfig != null) {
				connection = globalConfig.getHostConnection(tedBuilder.getConnectionId());
			}
			if (connection != null) {
				args.add(ExecutionCommandArguments.getExecutionHost()[1]).add(connection.getHost());
				args.add(ExecutionCommandArguments.getExecutionHostPort()[1]).add(connection.getPort());
				args.add(ExecutionCommandArguments.getCCSID()[1]).add(connection.getCodePage());
			}
		}
	}

	/**
	 * @param args
	 */
	private void addHostCredentialsArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getIncludeCred()) {
			String hostCreds = tedBuilder.getCredentialsId();
			if (!Strings.isNullOrEmpty(hostCreds)) {
				args.add(ExecutionCommandArguments.getHCIUserID()[1])
						.add(TEDExecutionRunnerUtils.getLoginInformation(build.getParent(), hostCreds).getUsername(), false);
				args.add(ExecutionCommandArguments.getHCIPassword()[1])
						.add(TEDExecutionRunnerUtils.getLoginInformation(build.getParent(), hostCreds).getPassword(), true);
			}
		}
	}

	/**
	 * @param args
	 */
	private void addJCLJobcardArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineJobcard()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine1())) {
				args.add(ExecutionCommandArguments.getJCLJobcard1()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine1()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine2())) {
				args.add(ExecutionCommandArguments.getJCLJobcard2()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine2()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine3())) {
				args.add(ExecutionCommandArguments.getJCLJobcard3()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine3()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine4())) {
				args.add(ExecutionCommandArguments.getJCLJobcard4()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine4()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getJclJobcardLine5())) {
				args.add(ExecutionCommandArguments.getJCLJobcard5()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getJclJobcardLine5()));
			}
		}
	}

	/**
	 * @param args
	 */
	private void addDatasetQualifierArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineQualifiers()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getDatasetHighLevelQualifier())) {
				args.add(ExecutionCommandArguments.getDatasetHLQ()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDatasetHighLevelQualifier()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getTemporaryDatasetPrefix())) {
				args.add(ExecutionCommandArguments.getTempDatasetPrefix()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getTemporaryDatasetPrefix()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getTemporaryDatasetSuffix())) {
				args.add(ExecutionCommandArguments.getTempDatasetSuffix()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getTemporaryDatasetSuffix()));
			}
		}
	}

	/**
	 * @param args
	 */
	private void addDataPrivacyOverrideArguments(final ArgumentListBuilder args) {
		if (tedBuilder.getDefineDataprivacyOverride()) {
			if (!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFADEBUG())) {
				args.add(ExecutionCommandArguments.getFADEBUG()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFADEBUG()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAEXPATH())) {
				args.add(ExecutionCommandArguments.getFAEXPATH()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAEXPATH()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAIPADDR())) {
				args.add(ExecutionCommandArguments.getFAIPADDR()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAIPADDR()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAJOPTS())) {
				args.add(ExecutionCommandArguments.getFAJOPTS()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAJOPTS()));
			}
			if (!Strings.isNullOrEmpty(tedBuilder.getDpOverrideFAJPATH())) {
				args.add(ExecutionCommandArguments.getFAJPATH()[1])
						.add(TEDExecutionRunnerUtils.escapeForScript(tedBuilder.getDpOverrideFAJPATH()));
			}
		}
	}
}
