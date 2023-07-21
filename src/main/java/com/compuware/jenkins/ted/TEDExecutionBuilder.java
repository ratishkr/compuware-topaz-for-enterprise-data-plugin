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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.compuware.jenkins.common.configuration.CpwrGlobalConfiguration;
import com.compuware.jenkins.common.configuration.HostConnection;
import com.google.common.base.Strings;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

/**
 * {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link TEDExecutionRunner} is created. The created instance is persisted to the
 * project configuration XML by using XStream, so this allows you to use
 * instance fields to remember remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform} method will be invoked.
 *
 */
public class TEDExecutionBuilder extends Builder implements SimpleBuildStep {
	
	/**
	 * Name of repository containing the specification to be executed. 
	 */
	private String repositoryName = DescriptorImpl.defaultRepositoryName;
	
	/**
	 * Name of the results repository where compare pro execution results will be stored. 
	 */
	private String resultsRepositoryName = DescriptorImpl.defaultResultsRepositoryName;

	/**
	 * Radio choice denoting whether single or multi specification execution.
	 */
	private String selectExecutionTypeRadio = DescriptorImpl.selectSingleSpecExecutionValue;
	
	/**
	 * Name of the specification to be executed.
	 */
	private String specificationName = DescriptorImpl.defaultSpecificationName;
	
	/**
	 * Type of the specification to be executed.
	 */
	private String specificationType = DescriptorImpl.defaultSpecificationType; 
	
	/**
	 * The specification list to be executed in case of multi-spec execution.
	 */
	private String specificationList = DescriptorImpl.defaultSpecificationList;
	
	/**
	 * The execution context file to be used. 
	 */
	private String executionContext = DescriptorImpl.defaultExecutionContext;
	
	/**
	 * The custom execution timeout. 
	 */
	private String executionTimeout = DescriptorImpl.defaultExecutionTimeout;
	
	/**
	 * If specification execution has to exit on encountering failure in case of multi-spec execution.
	 */
	private boolean exitOnFailure = DescriptorImpl.defaultExitOnFailure;

	/**
	 * If CES is being configured via the jenkins plugin.
	 */
	private boolean defineCES = DescriptorImpl.defaultDefineCES;
	
	/**
	 * The URL for the CES to be used for license check. 
	 */
	private String cesURL;
	
	/**
	 * If the CES URL provided is that of the BMC Cloud CES. 
	 */
	private boolean useCloudCES = DescriptorImpl.defaultUseCloud;
	
	/**
	 * The customer number for cloud CES  
	 */
	private String cloudCustomerNo = DescriptorImpl.defaultCloudCustomerNo;
	
	/**
	 * The site ID for cloud CES. 
	 */
	private String cloudSiteID = DescriptorImpl.defaultCloudSiteID;

	/**
	 * If communication manager is to be defined via the jenkins plugin.
	 */
	private boolean defineManager = DescriptorImpl.defaultDefineManager;
	
	/**
	 * Communication Manager host name or ip.
	 */
	private String communicationManager = DescriptorImpl.defaultCommunicationManager;
	
	/**
	 * Communication Manager's port.
	 */
	private String communicationManagerPort = DescriptorImpl.defaultCommunicationManagerPort;
	
	/**
	 * If execution server is to be defined via the jenkins plugin.
	 */
	private boolean defineServer = DescriptorImpl.defaultDefineServer;
	
	/**
	 * Execution Server Host name or ip.
	 */
	private String executionServer = DescriptorImpl.defaultExecutionServer;
	
	/**
	 * Execution Server's port.
	 */
	private String executionServerPort = DescriptorImpl.defaultExecutionServerPort;

	/**
	 * If execution host is to be defined via the jenkins plugin.
	 */
	private boolean defineHost = DescriptorImpl.defaultDefineHost;

	/**
	 * Execution Host Connection ID 
	 */
	//Host, port, CCSID should come from this.
	private String connectionId;  
	
	/**
	 * If execution host credentials is to be defined via the jenkins plugin.
	 */
	private boolean includeCred = DescriptorImpl.defaultIncludeCred;

	/**
	 * Execution Host - Credentials ID 
	 */
	//User ID, pwd should come from this. No support for keystore, and certificate via jenkins yet. 
	private String credentialsId; 
	
	/**
	 * If jobcard preferences are to be defined via the jenkins plugin.
	 */
	private boolean defineJobcard = DescriptorImpl.defaultDefineJobcard;
	
	/**
	 * JCL Jobcard Line 1. 
	 */
	private String jclJobcardLine1 = DescriptorImpl.defaultJCLJobcardLine1;
	
	/**
	 * JCL Jobcard Line 2.
	 */
	private String jclJobcardLine2 = DescriptorImpl.defaultJCLJobcardLine2;
	
	/**
	 * JCL Jobcard Line 3
	 */
	private String jclJobcardLine3 = DescriptorImpl.defaultJCLJobcardLine3;
	
	/**
	 * JCL Jobcard Line 4. 
	 */
	private String jclJobcardLine4 = DescriptorImpl.defaultJCLJobcardLine4;
	
	/**
	 * JCL Jobcard Line 5. 
	 */
	private String jclJobcardLine5 = DescriptorImpl.defaultJCLJobcardLine5;

	/**
	 * If dataset qualifiers are to be defined via the jenkins plugin.
	 */
	private boolean defineQualifiers = DescriptorImpl.defaultDefineQualifiers;
	
	/**
	 * Highlevel Qualifier to be used for Datasets.
	 */
	private String datasetHighLevelQualifier = DescriptorImpl.defaultDatasetHighLevelQualifier;
	
	/**
	 * Prefix to be used for temporary dataset.  
	 */
	private String temporaryDatasetPrefix = DescriptorImpl.defaultTemporaryDatasetPrefix;
	
	/**
	 * Suffix to be used for temporary dataset.
	 */
	private String temporaryDatasetSuffix = DescriptorImpl.defaultTemporaryDatasetSuffix;

	/**
	 * If dataprivacy overrides are to be defined via the jenkins plugin.
	 */
	private boolean defineDataprivacyOverride = DescriptorImpl.defaultDefineDataprivacyOverride;
	
	/**
	 * Data Privacy Override FADEBUG.
	 */
	private String dpOverrideFADEBUG = DescriptorImpl.defaultDpOverrideFADEBUG;
	
	/**
	 * Data Privacy Override FAEXPATH.
	 */
	private String dpOverrideFAEXPATH = DescriptorImpl.defaultDpOverrideFAEXPATH;
	
	/**
	 * Data Privacy Override FAIPADDR.
	 */
	private String dpOverrideFAIPADDR = DescriptorImpl.defaultDpOverrideFAJPADDR;
	
	/**
	 * Data Privacy Override FAJOPTS.
	 */
	private String dpOverrideFAJOPTS = DescriptorImpl.defaultDpOverrideFAJOPTS;
	
	/**
	 * Data Privacy Override FAJPATH.
	 */
	private String dpOverrideFAJPATH = DescriptorImpl.defaultDpOverrideFAJPATH;
	
	/**
	 * Flag to denote if the pipeline is to be halted on Failure.
	 */
	private boolean haltPipelineOnFailure = DescriptorImpl.defaultHaltPipelineOnFailure;
	
	/**
	 * Constructor for TED Execution Builder 
	 * 
	 * @param repositoryName
	 * 		Name of repository containing the specification to be executed
	 * @param resultsRepositoryName
	 * 		Name of the results repository where compare pro execution results will be stored
	 * @param selectExecutionTypeRadio
	 * 		Radio choice denoting whether single or multi specification execution
	 * @param specificationName
	 * 		Name of the specification to be executed
	 * @param specificationType
	 * 		Type of the specification to be executed
	 * @param specificationList
	 * 		The specification list to be executed in case of multi-spec execution
	 * @param exitOnFailure
	 * 		The execution context file to be used	
	 * @param executionContext
	 * 		The custom execution timeout
	 * @param executionTimeout
	 * 		If specification execution has to exit on encountering failure in case of multi-spec execution
	 * @param defineCES
	 * 		If CES is being configured via the jenkins plugin
	 * @param cesURL
	 * 		The URL for the CES to be used for license check
	 * @param useCloudCES
	 * 		If the CES URL provided is that of the BMC Cloud CES
	 * @param cloudCustomerNo
	 * 		The customer number for cloud CES 
	 * @param cloudSiteID
	 * 		The site ID for cloud CES
	 * @param defineManager
	 * 		If communication manager is to be defined via the jenkins plugin
	 * @param communicationManager
	 * 		Communication Manager host name or ip
	 * @param communicationManagerPort
	 * 		Communication Manager's port
	 * @param defineServer
	 * 		If execution server is to be defined via the jenkins plugin
	 * @param executionServer	
	 * 		Execution Server Host name or ip
	 * @param executionServerPort	
	 * 		Execution Server's port
	 * @param defineHost	
	 * 		If execution host is to be defined via the jenkins plugin
	 * @param connectionId
	 * 		 Execution Host Connection ID 
	 * @param includeCred
	 * 		If execution host credentials is to be defined via the jenkins plugin
	 * @param credentialsID
	 * 		Execution Host - Credentials ID 
	 * @param defineJobcard
	 * 		If jobcard preferences are to be defined via the jenkins plugin
	 * @param jclJobcardLine1
	 *		JCL Jobcard Line 1
	 * @param jclJobcardLine2
	 * 		JCL Jobcard Line 2	
	 * @param jclJobcardLine3
	 * 		JCL Jobcard Line 3
	 * @param jclJobcardLine4
	 * 		JCL Jobcard Line 4
	 * @param jclJobcardLine5
	 * 		JCL Jobcard Line 5
	 * @param defineQualifiers
	 * 		If dataset qualifiers are to be defined via the jenkins plugin
	 * @param datasetHighLevelQualifier
	 * 		Highlevel qualifiers to be used for Datasets
	 * @param temporaryDatasetPrefix
	 * 		Prefix to be used for temporary datasets
	 * @param temporaryDatasetSuffix
	 * 		Suffix to be used for temporary datasets
	 * @param defineDataprivacyOverride
	 * 		If dataprivacy overrides are to be defined via the jenkins plugin
	 * @param dpOverrideFADEBUG
	 * 		Data Privacy Override FADEBUD
	 * @param dpOverrideFAEXPATH
	 * 		Data Privacy Override FAEXPATH
	 * @param dpOverrideFAIPADDR
	 * 		Data Privacy Override FAIPADDR
	 * @param dpOverrideFAJOPTS
	 * 		Data Privacy Override FAJOPTS
	 * @param dpOverrideFAJPATH
	 * 		Data Privacy Override FAJPATH
	 * @param haltPipelineOnFailure
	 * 		Flag to denote if the pipeline is to be halted on Failure
	 */
	@DataBoundConstructor
	public TEDExecutionBuilder(String repositoryName, String resultsRepositoryName, 
			String selectExecutionTypeRadio, String specificationName, String specificationType, 
			String specificationList, boolean exitOnFailure, 
			String executionContext, String executionTimeout, 
			boolean defineCES, String cesURL, boolean useCloudCES, String cloudCustomerNo, String cloudSiteID,
			boolean defineManager, String communicationManager, String communicationManagerPort, 
			boolean defineServer, String executionServer, String executionServerPort, 
			boolean defineHost, String connectionId, boolean includeCred, String credentialsID,
			boolean defineJobcard, String jclJobcardLine1, String jclJobcardLine2, String jclJobcardLine3, String jclJobcardLine4, String jclJobcardLine5,
			boolean defineQualifiers, String datasetHighLevelQualifier, String temporaryDatasetPrefix, String temporaryDatasetSuffix,
			boolean defineDataprivacyOverride, String dpOverrideFADEBUG, String dpOverrideFAEXPATH, String dpOverrideFAIPADDR, String dpOverrideFAJOPTS, String dpOverrideFAJPATH, 
			boolean haltPipelineOnFailure) { 
		super();
		this.repositoryName = repositoryName;
		this.resultsRepositoryName = resultsRepositoryName;

		this.selectExecutionTypeRadio = selectExecutionTypeRadio;
		
		this.specificationName = specificationName;
		this.specificationType = specificationType;
		this.specificationList = specificationList;
		this.exitOnFailure = exitOnFailure;
		
		this.executionContext = executionContext;
		this.executionTimeout = executionTimeout;
		
		this.defineCES = defineCES;
		this.useCloudCES = useCloudCES;
		this.cesURL = cesURL;
		this.cloudCustomerNo = cloudCustomerNo;
		this.cloudSiteID = cloudSiteID;

		this.defineManager = defineManager;
		this.communicationManager = communicationManager;
		this.communicationManagerPort = communicationManagerPort;
		
		this.defineServer = defineServer;
		this.executionServer = executionServer;
		this.executionServerPort = executionServerPort;
		
		this.defineHost = defineHost;
		this.connectionId = connectionId;
		this.includeCred = includeCred;
		this.credentialsId = credentialsID;
		
		this.defineJobcard = defineJobcard;
		this.jclJobcardLine1 = jclJobcardLine1;
		this.jclJobcardLine2 = jclJobcardLine2;
		this.jclJobcardLine3 = jclJobcardLine3;
		this.jclJobcardLine4 = jclJobcardLine4;
		this.jclJobcardLine5 = jclJobcardLine5;
		
		this.defineQualifiers = defineQualifiers;
		this.datasetHighLevelQualifier = datasetHighLevelQualifier;
		this.temporaryDatasetPrefix = temporaryDatasetPrefix;
		this.temporaryDatasetSuffix = temporaryDatasetSuffix;
		
		this.defineDataprivacyOverride = defineDataprivacyOverride;
		this.dpOverrideFADEBUG = dpOverrideFADEBUG;
		this.dpOverrideFAEXPATH = dpOverrideFAEXPATH;
		this.dpOverrideFAIPADDR = dpOverrideFAIPADDR;
		this.dpOverrideFAJOPTS = dpOverrideFAJOPTS;
		this.dpOverrideFAJPATH = dpOverrideFAJPATH;
		
		this.haltPipelineOnFailure = haltPipelineOnFailure;
	}
	
	/**
	 * Performs the execution of the pipeline.
	 */
	@Override
	public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException{
		listener.getLogger().println("Running " + Messages.displayName() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

		try {
			validateParameters(launcher, listener, build.getParent());

			TEDExecutionRunner runner = new TEDExecutionRunner(this);
			boolean success = runner.run(build, launcher, workspace, listener);
			
			if (success) {
				listener.getLogger().println("Execution Success..."); //$NON-NLS-1$
			} else {
				listener.error("Execution failure"); //$NON-NLS-1$
				throw new AbortException("Execution failure"); //$NON-NLS-1$
			}

		} catch (Exception e) {
			listener.getLogger().println(e.getMessage());
			throw new AbortException();
		}
	}
	
	/**
	 * Validates the defined parameters.
	 * 
	 * @param launcher launcher
	 * @param listener tasklistener
	 * @param project  project
	 */
	public void validateParameters(final Launcher launcher, final TaskListener listener, final Item project) {
		if(isSingleSpecExecution()) {
			if (Strings.isNullOrEmpty(this.specificationName)) {
				throw new IllegalArgumentException("No specification name provided. Enter a specification name."); //$NON-NLS-1$
			}
			if (Strings.isNullOrEmpty(this.specificationType)) {
				throw new IllegalArgumentException("Type of the specification that is to be executed is not selected. Select the specification type."); //$NON-NLS-1$
			}
		} else {
			if (Strings.isNullOrEmpty(this.specificationList)) {
				throw new IllegalArgumentException("Specification list is not provided. Enter the list of space separated specification name and type."); //$NON-NLS-1$
			}
		}
		
		if(getDefineHost() && getConnectionId()!=null && !getConnectionId().isEmpty()) {
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();

			HostConnection connection = null;
			if (globalConfig != null) {
				connection = globalConfig.getHostConnection(getConnectionId());
			}
			if (connection == null) {
				throw new IllegalArgumentException("No host connection defined. Check project and global configurations to make sure a valid host connection is set."); //$NON-NLS-1$
			}
			if(getIncludeCred()) {
				if (getCredentialsId().isEmpty()) {
					throw new IllegalArgumentException("Missing Credentials ID - configure plugin correctly."); //$NON-NLS-1$
				} else {
					if (TEDExecutionRunnerUtils.getLoginInformation(project, getCredentialsId()) != null) {
						listener.getLogger().println("Credentials entered..."); //$NON-NLS-1$
					} else {
						throw new IllegalArgumentException("Credential ID entered is not valid - enter valid ID from Jenkins Credentials plugin"); //$NON-NLS-1$
					}
				}
			}
		}
		if(getDefineCES()) {
			if(Strings.isNullOrEmpty(getCesURL())) {
				throw new IllegalArgumentException("A valid CES url needs to be provided.");				
			}
			if(getUseCloudCES() && 
					(Strings.isNullOrEmpty(getCloudSiteID()) || Strings.isNullOrEmpty(getCloudCustomerNo()))) {
				throw new IllegalArgumentException("Since Cloud CES Type is chosen, Site ID and Customer Number needs to be provided.");					
			}
		}
	}
	
	@Override
	public Descriptor<Builder> getDescriptor() {
		return new DescriptorImpl();
	}
	
	/**
	 * Returns the selected execution type radio button.
	 * 
	 * @return	<code>String</code> value of the selectProgramsRadio option.
	 */
	public String getSelectExecutionType() {
		String selectedExecutionType = null;
		
		if (isSingleSpecExecution()) {
			selectedExecutionType =  DescriptorImpl.selectSingleSpecExecutionValue;
		
		} else if (isMultiSpecExecution()) {
			selectedExecutionType =  DescriptorImpl.selectMultiSpecExecutionValue;
		}
		
		return selectedExecutionType;
	}
	
	/**
	 * Returns if Single Spec Execution is selected.
	 * 
	 * @return	<code>true</code> if Select JSON option is selected, otherwise <code>false</code>.
	 */
	public boolean isSingleSpecExecution() {
		return Strings.isNullOrEmpty(selectExecutionTypeRadio) ||
				selectExecutionTypeRadio.compareTo(DescriptorImpl.selectSingleSpecExecutionValue) == 0;
    }
	
	/**
	 * Returns if Multi Spec Execution is selected.
	 * 
	 * @return	<code>true</code> if Select JSON option is selected, otherwise <code>false</code>.
	 */
	public boolean isMultiSpecExecution() {
		return Strings.isNullOrEmpty(selectExecutionTypeRadio) ||
				selectExecutionTypeRadio.compareTo(DescriptorImpl.selectMultiSpecExecutionValue) == 0;
    }

	/**
	 * Returns the name of the repository name to be used for specification retrieval.
	 * 
	 * @return <code>String</code>
	 */
	public String getRepositoryName() {
		return repositoryName;
	}

	/**
	 * Sets the name of the repository name to be used for specification retrieval.
	 * 
	 * @param repositoryName <code>String</code>
	 */
	@DataBoundSetter
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	/**
	 * Returns the name of the results repository name to be used for storing compare results.
	 * 
	 * @return <code>String</code>
	 */
	public String getResultsRepositoryName() {
		return resultsRepositoryName;
	}

	/**
	 * Sets the name of the repository name to be used for storing compare results.
	 *  
	 * @param resultsRepositoryName <code>String</code>
	 */
	@DataBoundSetter
	public void setResultsRepositoryName(String resultsRepositoryName) {
		this.resultsRepositoryName = resultsRepositoryName;
	}

	/**
	 * Returns the chosen execution type.
	 * 
	 * @return <code>String</code>
	 */
	public String getSelectExecutionTypeRadio() {
		return selectExecutionTypeRadio;
	}

	/**
	 * Sets the chosen execution type.
	 * 
	 * @param selectExecutionTypeRadio <code>String</code>
	 */
	@DataBoundSetter
	public void setSelectExecutionTypeRadio(String selectExecutionTypeRadio) {
		this.selectExecutionTypeRadio = selectExecutionTypeRadio;
	}

	/**
	 * 	Returns the name of the specification to be executed.

	 * @return <code>String</code>
	 */
	public String getSpecificationName() {
		return specificationName;
	}

	/**
	 * Sets the name of the specification to be executed.
	 * 
	 * @param specificationName <code>String</code>
	 */
	@DataBoundSetter
	public void setSpecificationName(String specificationName) {
		this.specificationName = specificationName;
	}

	/**
	 * Returns the type of the specification to be executed.
	 * 
	 * @return <code>String</code>
	 */
	public String getSpecificationType() {
		return specificationType;
	}

	/**
	 * Sets the type of the specification to be executed.
	 * 
	 * @param specificationType <code>String</code>
	 */
	@DataBoundSetter
	public void setSpecificationType(String specificationType) {
		this.specificationType = specificationType;
	}

	/**
	 * Returns the list specifications to be executed.
	 * 
	 * @return <code>String</code>
	 */
	public String getSpecificationList() {
		return specificationList;
	}

	/**
	 * Sets the list specifications to be executed.
	 * 
	 * @param specificationList <code>String</code>
	 */
	@DataBoundSetter
	public void setSpecificationList(String specificationList) {
		this.specificationList = specificationList;
	}

	/**
	 * Returns the execution context to be used.
	 * 
	 * @return <code>String</code>
	 */
	public String getExecutionContext() {
		return executionContext;
	}

	/**
	 * Sets the execution context to be used.
	 * 
	 * @param executionContext <code>String</code>
	 */
	@DataBoundSetter
	public void setExecutionContext(String executionContext) {
		this.executionContext = executionContext;
	}
	
	/**
	 * Returns the flag if process has to exit on failure after a specification fails in a list.
	 *  
	 * @return <code>boolean</code>
	 */
	public boolean getExitOnFailure() {
		return exitOnFailure;
	}

	/**
	 * Sets the flag if process has to exit on failure after a specification fails in a list.
	 * 
	 * @param exitOnFailure <code>boolean</code>
	 */
	@DataBoundSetter
	public void setExitOnFailure(boolean exitOnFailure) {
		this.exitOnFailure = exitOnFailure;
	}

	/**
	 * Returns the value for the execution timeout.
	 *  
	 * @return <code>String</code>
	 */
	public String getExecutionTimeout() {
		return executionTimeout;
	}

	/**
	 * Sets the value for the execution timeout.
	 * 
	 * @param executionTimeout <code>String</code>
	 */
	@DataBoundSetter
	public void setExecutionTimeout(String executionTimeout) {
		this.executionTimeout = executionTimeout;
	}

	/**
	 * Returns if CES is being defined.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineCES() {
		return defineCES;
	}

	/**
	 * Sets if the CES is being defined.
	 * 
	 * @param defineCES <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineCES(boolean defineCES) {
		this.defineCES = defineCES;
	}

	/**
	 * Returns if Cloud CES is to be used.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getUseCloudCES() {
		return useCloudCES;
	}

	/**
	 * Set if the Cloud CES is to be used.
	 * 
	 * @param useCloudCES <code>boolean</code>
	 */
	@DataBoundSetter
	public void setUseCloudCES(boolean useCloudCES) {
		this.useCloudCES = useCloudCES;
	}

	/**
	 * Returns the CES URL.
	 * 
	 * @return <code>String</code>
	 */
	public String getCesURL() {
		return cesURL;
	}

	/**
	 * Sets the CES URL.
	 * 
	 * @param cesURL <code>String</code>
	 */
	@DataBoundSetter
	public void setCesURL(String cesURL) {
		this.cesURL = cesURL;
	}

	/**
	 * Returns the cloud customer number.
	 *  
	 * @return <code>String</code>
	 */
	public String getCloudCustomerNo() {
		return cloudCustomerNo;
	}

	/**
	 * Sets the cloud customer number. 
	 * 
	 * @param cloudCustomerNo <code>String</code>
	 */
	@DataBoundSetter
	public void setCloudCustomerNo(String cloudCustomerNo) {
		this.cloudCustomerNo = cloudCustomerNo;
	}

	/**
	 * Returns the cloud site id.
	 * 
	 * @return <code>String</code>
	 */
	public String getCloudSiteID() {
		return cloudSiteID;
	}

	/**
	 * Sets the cloud site id.
	 * 
	 * @param cloudSiteID <code>String</code>
	 */
	@DataBoundSetter
	public void setCloudSiteID(String cloudSiteID) {
		this.cloudSiteID = cloudSiteID;
	}

	/**
	 * Returns if the communication manager is being defined.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineManager() {
		return defineManager;
	}

	/**
	 * Set if the communication manager is being defined.
	 * 
	 * @param defineManager <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineManager(boolean defineManager) {
		this.defineManager = defineManager;
	}

	/**
	 * Returns the communication manager host.
	 * 
	 * @return <code>String</code>
	 */
	public String getCommunicationManager() {
		return communicationManager;
	}

	/**
	 * Set the communication manager host.
	 * 
	 * @param communicationManager <code>String</code>
	 */
	@DataBoundSetter
	public void setCommunicationManager(String communicationManager) {
		this.communicationManager = communicationManager;
	}

	/**
	 * Returns the communication manager port.
	 * 
	 * @return <code>String</code>
	 */
	public String getCommunicationManagerPort() {
		return communicationManagerPort;
	}

	/**
	 * Set the communication manager port.
	 * 
	 * @param communicationManagerPort <code>String</code>
	 */
	@DataBoundSetter
	public void setCommunicationManagerPort(String communicationManagerPort) {
		this.communicationManagerPort = communicationManagerPort;
	}

	/**
	 * Return if the execution server is to be defined.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineServer() {
		return defineServer;
	}

	/**
	 * Set if the execution server is to be defined.
	 * 
	 * @param defineServer <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineServer(boolean defineServer) {
		this.defineServer = defineServer;
	}

	/**
	 * Returns the execution server host.
	 * 
	 * @return <code>String</code>
	 */
	public String getExecutionServer() {
		return executionServer;
	}

	/**
	 * Sets the execution server host.
	 * 
	 * @param executionServer <code>String</code>
	 */
	@DataBoundSetter
	public void setExecutionServer(String executionServer) {
		this.executionServer = executionServer;
	}

	/**
	 * Returns the execution server port.
	 * 
	 * @return <code>String</code>
	 */
	public String getExecutionServerPort() {
		return executionServerPort;
	}

	/**
	 * Sets the execution server port.
	 * 
	 * @param executionServerPort <code>String</code>
	 */
	@DataBoundSetter
	public void setExecutionServerPort(String executionServerPort) {
		this.executionServerPort = executionServerPort;
	}
	
	/**
	 * Returns if the execution host has to be defined.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineHost() {
		return defineHost;
	}

	/**
	 * Sets if the execution host has to be defined.
	 * 
	 * @param defineHost <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineHost(boolean defineHost) {
		this.defineHost = defineHost;
	}

	/**
	 * Returns the execution host connection id.
	 * 
	 * @return <code>String</code>
	 */
	public String getConnectionId() {
		return connectionId;
	}

	/**
	 * Sets the execution host connection id.
	 * 
	 * @param connectionId <code>String</code>
	 */
	@DataBoundSetter
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	/**
	 * Returns if the execution host credentials has to be included
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getIncludeCred() {
		return includeCred;
	}

	/**
	 * Sets if the execution host credentials has to be included
	 * 
	 * @param includeCred <code>boolean</code>
	 */
	@DataBoundSetter
	public void setIncludeCred(boolean includeCred) {
		this.includeCred = includeCred;
	}

	/**
	 * Returns the execution host credentials
	 * 
	 * @return <code>String</code>
	 */
	public String getCredentialsId() {
		return credentialsId;
	}

	/**
	 * Sets the execution host credentials
	 * 
	 * @param credentialsId <code>String</code> 
	 */
	@DataBoundSetter
	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}

	/**
	 * Returns the if the JCL Jobcard preferences are to be defined.
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineJobcard() {
		return defineJobcard;
	}

	/**
	 * Sets the if the JCL Jobcard preferences are to be defined.
	 * 
	 * @param defineJobcard <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineJobcard(boolean defineJobcard) {
		this.defineJobcard = defineJobcard;
	}

	/**
	 * Returns the JCL Jobcard preference line 1
	 *  
	 * @return <code>String</code>
	 */
	public String getJclJobcardLine1() {
		return jclJobcardLine1;
	}

	/**
	 * Sets the JCL Jobcard preference line 1
	 * 
	 * @param jclJobcardLine1 <code>String</code>
	 */
	@DataBoundSetter
	public void setJclJobcardLine1(String jclJobcardLine1) {
		this.jclJobcardLine1 = jclJobcardLine1;
	}

	/**
	 * Returns the JCL Jobcard preference line 2
	 *  
	 * @return <code>String</code>
	 */
	public String getJclJobcardLine2() {
		return jclJobcardLine2;
	}

	/**
	 * Sets the JCL Jobcard preference line 2
	 * 
	 * @param jclJobcardLine2 <code>String</code>
	 */
	@DataBoundSetter
	public void setJclJobcardLine2(String jclJobcardLine2) {
		this.jclJobcardLine2 = jclJobcardLine2;
	}

	/**
	 * Returns the JCL Jobcard preference line 3
	 *  
	 * @return <code>String</code>
	 */
	public String getJclJobcardLine3() {
		return jclJobcardLine3;
	}

	/**
	 * Sets the JCL Jobcard preference line 3
	 * 
	 * @param jclJobcardLine3 <code>String</code>
	 */
	@DataBoundSetter
	public void setJclJobcardLine3(String jclJobcardLine3) {
		this.jclJobcardLine3 = jclJobcardLine3;
	}

	/**
	 * Returns the JCL Jobcard preference line 4
	 *  
	 * @return <code>String</code>
	 */
	public String getJclJobcardLine4() {
		return jclJobcardLine4;
	}

	/**
	 * Sets the JCL Jobcard preference line 4
	 * 
	 * @param jclJobcardLine4 <code>String</code>
	 */
	@DataBoundSetter
	public void setJclJobcardLine4(String jclJobcardLine4) {
		this.jclJobcardLine4 = jclJobcardLine4;
	}

	/**
	 * Returns the JCL Jobcard preference line 5
	 *  
	 * @return <code>String</code>
	 */
	public String getJclJobcardLine5() {
		return jclJobcardLine5;
	}

	/**
	 * Sets the JCL Jobcard preference line 5
	 * 
	 * @param jclJobcardLine5 <code>String</code>
	 */
	@DataBoundSetter
	public void setJclJobcardLine5(String jclJobcardLine5) {
		this.jclJobcardLine5 = jclJobcardLine5;
	}

	/**
	 * Returns if the dataset qualifiers are to be defined
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineQualifiers() {
		return defineQualifiers;
	}

	/**
	 * Sets if the dataset qualifiers are to be defined
	 * 
	 * @param defineQualifiers <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineQualifiers(boolean defineQualifiers) {
		this.defineQualifiers = defineQualifiers;
	}

	/**
	 * Returns the dataset highlevel qualifier
	 * 
	 * @return <code>String</code>
	 */
	public String getDatasetHighLevelQualifier() {
		return datasetHighLevelQualifier;
	}

	/**
	 * Sets the dataset highlevel qualifier
	 * 
	 * @param datasetHighLevelQualifier <code>String</code>
	 */
	@DataBoundSetter
	public void setDatasetHighLevelQualifier(String datasetHighLevelQualifier) {
		this.datasetHighLevelQualifier = datasetHighLevelQualifier;
	}

	/**
	 * Returns the temporary dataset prefix
	 * 
	 * @return <code>String</code>
	 */
	public String getTemporaryDatasetPrefix() {
		return temporaryDatasetPrefix;
	}

	/**
	 * Sets the temporary dataset prefix
	 * 
	 * @param temporaryDatasetPrefix <code>String</code>
	 */
	@DataBoundSetter
	public void setTemporaryDatasetPrefix(String temporaryDatasetPrefix) {
		this.temporaryDatasetPrefix = temporaryDatasetPrefix;
	}

	/**
	 * Returns the temporary dataset suffix
	 * 
	 * @return <code>String</code>
	 */
	public String getTemporaryDatasetSuffix() {
		return temporaryDatasetSuffix;
	}

	/**
	 * Sets the temporary dataset suffix
	 * 
	 * @param temporaryDatasetSuffix <code>String</code>
	 */
	@DataBoundSetter
	public void setTemporaryDatasetSuffix(String temporaryDatasetSuffix) {
		this.temporaryDatasetSuffix = temporaryDatasetSuffix;
	}

	/**
	 * Returns if the dataprivacy overrides is defined
	 * 
	 * @return <code>boolean</code>
	 */
	public boolean getDefineDataprivacyOverride() {
		return defineDataprivacyOverride;
	}

	/**
	 * Sets if the dataprivacy overrides is defined
	 * 
	 * @param defineDataprivacyOverride <code>boolean</code>
	 */
	@DataBoundSetter
	public void setDefineDataprivacyOverride(boolean defineDataprivacyOverride) {
		this.defineDataprivacyOverride = defineDataprivacyOverride;
	}

	/**
	 * Returns the dataprivacy overrides FADEBUG 
	 * 
	 * @return <code>String</code>
	 */
	public String getDpOverrideFADEBUG() {
		return dpOverrideFADEBUG;
	}

	/**
	 * Sets the dataprivacy overrides FADEBUG
	 * 
	 * @param dpOverrideFADEBUG <code>String</code>
	 */
	@DataBoundSetter
	public void setDpOverrideFADEBUG(String dpOverrideFADEBUG) {
		this.dpOverrideFADEBUG = dpOverrideFADEBUG;
	}

	/**
	 * Returns the dataprivacy overrides FAEXPATH
	 * 
	 * @return <code>String</code>
	 */
	public String getDpOverrideFAEXPATH() {
		return dpOverrideFAEXPATH;
	}

	/**
	 * Sets the dataprivacy overrides FAEXPATH
	 * 
	 * @param dpOverrideFAEXPATH <code>String</code>
	 */
	@DataBoundSetter
	public void setDpOverrideFAEXPATH(String dpOverrideFAEXPATH) {
		this.dpOverrideFAEXPATH = dpOverrideFAEXPATH;
	}

	/**
	 * Returns the dataprivacy overrides FAIPADDR
	 * 
	 * @return <code>String</code>
	 */
	public String getDpOverrideFAIPADDR() {
		return dpOverrideFAIPADDR;
	}

	/**
	 * Sets the dataprivacy overrides FAIPADDR
	 * 
	 * @param dpOverrideFAIPADDR <code>String</code>
	 */
	@DataBoundSetter
	public void setDpOverrideFAIPADDR(String dpOverrideFAIPADDR) {
		this.dpOverrideFAIPADDR = dpOverrideFAIPADDR;
	}

	/**
	 * Returns the dataprivacy overrides FAJOPTS
	 * 
	 * @return <code>String</code>
	 */
	public String getDpOverrideFAJOPTS() {
		return dpOverrideFAJOPTS;
	}

	/**
	 * Sets the dataprivacy overrides FAJOPTS
	 * 
	 * @param dpOverrideFAJOPTS <code>String</code>
	 */
	@DataBoundSetter
	public void setDpOverrideFAJOPTS(String dpOverrideFAJOPTS) {
		this.dpOverrideFAJOPTS = dpOverrideFAJOPTS;
	}

	/**
	 * Returns the dataprivacy overrides FAJPATH
	 * 
	 * @return <code>String</code>
	 */
	public String getDpOverrideFAJPATH() {
		return dpOverrideFAJPATH;
	}

	/**
	 * Sets the dataprivacy overrides FAJPATH
	 * 
	 * @param dpOverrideFAJPATH <code>String</code> 
	 */
	@DataBoundSetter
	public void setDpOverrideFAJPATH(String dpOverrideFAJPATH) {
		this.dpOverrideFAJPATH = dpOverrideFAJPATH;
	}

	/**
	 * Should the pipeline execution continues when an error occurs.
	 * 
	 * @return	<code>true</code> indicates pipeline should stop error is detected.
	 * 			<code>false</code> indicates pipeline should not stop error is detected.
	 */
	public boolean getHaltPipelineOnFailure() {
		return haltPipelineOnFailure;
	}

	/**
	 * Set if the pipeline execution continues when an error occurs.
	 * 
	 * @param haltPipelineOnFailure
	 * 			<code>true</code> indicates pipeline should stop error is detected.
	 * 			<code>false</code> indicates pipeline should not stop error is detected.
	 */
	@DataBoundSetter
	public void setHaltPipelineOnFailure(boolean haltPipelineOnFailure) {
		this.haltPipelineOnFailure = haltPipelineOnFailure;
	}

	/**
	 * Returns the associated text for the "Halt pipeline if errors occur" field's title
	 * 
	 * @return <code>String</code> The "Halt pipeline if errors occur" field title
	 */
	public final String getHaltPipelineTitle() {
		return DescriptorImpl.haltPipelineTitle;
	}
	

	@Symbol("ted")
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

		public static final String defaultRepositoryName = "";
		public static final String defaultResultsRepositoryName = "";
		
		public static final String defaultSpecificationName = "";
		public static final String defaultSpecificationType = SpecificationType.EMPTY.getDisplayName();
		
		public static final String defaultSpecificationList = "";
		
		public static final String defaultExecutionContext = "";
		public static final String defaultExecutionTimeout = "";
		public static final Boolean defaultExitOnFailure = true;

		public static final Boolean defaultDefineCES = false; 
		public static final Boolean defaultUseCloud = false;
		public static final String defaultCloudCustomerNo = "";
		public static final String defaultCloudSiteID = "";
		
		public static final boolean defaultDefineManager = false;
		public static final String defaultCommunicationManager = "";
		public static final String defaultCommunicationManagerPort = "";
		
		public static final boolean defaultDefineServer = false;
		public static final String defaultExecutionServer = "";
		public static final String defaultExecutionServerPort = "";
		
		public static final Boolean defaultDefineHost = false;
		public static final String defaultHostConnectionId = "";
		public static final Boolean defaultIncludeCred = false;
		public static final String defaultHostCredentialId = "";
		
		public static final boolean defaultDefineJobcard = false;
		public static final String defaultJCLJobcardLine1 = "";
		public static final String defaultJCLJobcardLine2 = "";
		public static final String defaultJCLJobcardLine3 = "";
		public static final String defaultJCLJobcardLine4 = "";
		public static final String defaultJCLJobcardLine5 = "";

		public static final boolean defaultDefineQualifiers = false;
		public static final String defaultDatasetHighLevelQualifier = "";
		public static final String defaultTemporaryDatasetPrefix = "";
		public static final String defaultTemporaryDatasetSuffix = "";

		public static final boolean defaultDefineDataprivacyOverride = false;
		public static final String defaultDpOverrideFADEBUG = "";
		public static final String defaultDpOverrideFAEXPATH = "";
		public static final String defaultDpOverrideFAJPADDR = "";
		public static final String defaultDpOverrideFAJOPTS = "";
		public static final String defaultDpOverrideFAJPATH = "";
		
		public static final String selectSingleSpecExecutionValue = "-sse";
		public static final String selectMultiSpecExecutionValue = "-mse";
		
		public static final Boolean defaultHaltPipelineOnFailure = true;
		public static final String haltPipelineTitle = "Halt pipeline if errors occur"; //NOSONAR
		
		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
			return true;
		}
		
		@Override
		public String getDisplayName() {
			return Messages.displayName();
		}
		
		/**
		 * @param context context
		 * @param specType specType
		 * @param project project
		 * @return <code>ListBoxModel</code>
		 */
		public ListBoxModel doFillSpecificationTypeItems(@AncestorInPath Jenkins context, @QueryParameter String specType, @AncestorInPath Item project) {
			ListBoxModel specTypeModel = new ListBoxModel();
			specTypeModel.add(new Option(SpecificationType.EMPTY.getDisplayName(), SpecificationType.EMPTY.getValue(), (specType == null || !SpecificationType.EMPTY.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.COMPARE.getDisplayName(), SpecificationType.COMPARE.getValue(), (specType == null || !SpecificationType.COMPARE.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.CONVERT.getDisplayName(), SpecificationType.CONVERT.getValue(), (specType == null || !SpecificationType.CONVERT.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.EXSUITE.getDisplayName(), SpecificationType.EXSUITE.getValue(), (specType == null || !SpecificationType.EXSUITE.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.EXTRACT.getDisplayName(), SpecificationType.EXTRACT.getValue(), (specType == null || !SpecificationType.EXTRACT.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.LOAD.getDisplayName(), SpecificationType.LOAD.getValue(), (specType == null || !SpecificationType.LOAD.getValue().equals(specType)? false : true))); //NOSONAR

			return specTypeModel;
		}
		
		/**
		 * Fills in the Host Connection selection box with applicable connections.
		 * 
		 * @param context
		 * 		An instance of <code>context</code> for the Jenkin's context
		 * @param connectionId
		 *            an existing host connection identifier; can be null
		 * @param project
		 * 		An instance of <code>Item</code> for the project.
		 * 
		 * @return host connection selections
		 */
		public ListBoxModel doFillConnectionIdItems(@AncestorInPath Jenkins context, @QueryParameter String connectionId,
				@AncestorInPath Item project) {
			if(project == null) {
				//Checking Permission for admin user
				Jenkins.get().checkPermission(Jenkins.ADMINISTER);
			}
			else {
				project.checkPermission(Item.CONFIGURE);				
			}
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
			HostConnection[] hostConnections = globalConfig.getHostConnections();

			ListBoxModel model = new ListBoxModel();
			model.add(new Option(StringUtils.EMPTY, StringUtils.EMPTY, false));

			for (HostConnection hostConnection : hostConnections)
			{
				boolean isSelected = false;
				if (connectionId != null)
				{
					isSelected = connectionId.matches(hostConnection.getConnectionId());
				}

				model.add(new Option(hostConnection.getDescription() + " [" + hostConnection.getHostPort() + ']', //$NON-NLS-1$
						hostConnection.getConnectionId(), isSelected));
			}

			return model;
		}
		
		/**
		 * Fills in the Login Credential selection box with applicable Jenkins credentials
		 * 
		 * @param context
		 *            Jenkins context.
		 * @param credentialsId
		 *            The host credential id for the user.
		 * @param project
		 *            The Jenkins project.
		 * 
		 * @return credential selections
		 * 
		 */
		public ListBoxModel doFillCredentialsIdItems(@AncestorInPath final Jenkins context, 
				@QueryParameter final String credentialsId, @AncestorInPath final Item project){
			if(project == null) {
				//Checking Permission for admin user
				Jenkins.get().checkPermission(Jenkins.ADMINISTER);
			}
			else {
				project.checkPermission(Item.CONFIGURE);				
			}
			List<StandardUsernamePasswordCredentials> creds = CredentialsProvider.lookupCredentials(
					StandardUsernamePasswordCredentials.class, project, ACL.SYSTEM, Collections.<DomainRequirement> emptyList());

			StandardListBoxModel model = new StandardListBoxModel();

			model.add(new Option("", "", false));

			for (StandardUsernamePasswordCredentials c : creds) {
				boolean isSelected = false;

				if (credentialsId != null) {
					isSelected = credentialsId.matches(c.getId());
				}

				String description = Util.fixEmptyAndTrim(c.getDescription());
				model.add(new Option(c.getUsername() + (description != null ? " (" + description + ")" : ""), c.getId(), isSelected));
			}

			return model;
		}
		
		/**
		 * Fills in the CES server URL selection box with applicable Jenkins credentials
		 * 
		 * @param cesUrl The cesUrl id for the user.
		 * 
		 * @return cesUrl selections
		 * 
		 */
		public ListBoxModel doFillCesURLItems(@QueryParameter String cesUrl, @AncestorInPath Item project) {
			if (project == null) {
				// Checking Permission for admin user
				Jenkins.get().checkPermission(Jenkins.ADMINISTER);
			}
			else {
				project.checkPermission(Item.CONFIGURE);				
			}
			ListBoxModel model = new ListBoxModel();
			model.add(new Option("", "", false));
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
			if (globalConfig != null) {
				HostConnection[] hostConnections = globalConfig.getHostConnections();

				for (HostConnection connection : hostConnections) {
					String cesServerURL = connection.getCesUrl();
					if (cesServerURL != null && !cesServerURL.isEmpty()) {
						boolean isSelected = false;
						if (cesUrl != null) {
							isSelected = cesUrl.equalsIgnoreCase(cesServerURL);
						}

						String cesValue = cesServerURL;
						Option opt = new Option(cesValue, cesServerURL, isSelected);
						boolean exists = false;

						ListIterator<Option> li = model.listIterator();
						while (li.hasNext()) {
							if (li.next().value.equalsIgnoreCase(cesServerURL)) {
								exists = true;
								break;
							}
						}

						if (!exists) {
							model.add(opt);
						}
					}
				}
			}

			return model;
		}

		
		public FormValidation doCheckRepositoryName(@QueryParameter String value) {
			if (value.length() == 0){
				return FormValidation.error(Messages.errors_missingRepositoryName());
			}
			return FormValidation.ok();
		}


		/**
		 * Validates for the 'Execution Timeout' field
		 * 
		 * @param value
		 * 		The code coverage threshold.
		 * @return validation message
		 */
		public FormValidation doCheckExecutionTimeOut(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.ok();
			}
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return FormValidation.error(Messages.errors_invalidExecutionTimeout());
			}

			return FormValidation.ok();
		}
		
		/**
		 * Validates for the 'Communication Manager Port' field
		 * 
		 * @param value
		 * 		The code coverage threshold.
		 * @return validation message
		 */
		public FormValidation doCheckCommunicationManagerPort(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.ok();
			}

			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return FormValidation.error(Messages.errors_invalidCommunicationManagerPort());
			}

			return FormValidation.ok();
		}
		
		/**
		 * Validates for the 'Execution Server Port' field
		 * 
		 * @param value
		 * 		The code coverage threshold.
		 * @return validation message
		 */
		public FormValidation doCheckExecutionServerPort(@QueryParameter String value) {
			if (value.length() == 0) {
				return FormValidation.ok();
			}

			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return FormValidation.error(Messages.errors_invalidExecutionServerPort());
			}

			return FormValidation.ok();
		}
		
	}
}
