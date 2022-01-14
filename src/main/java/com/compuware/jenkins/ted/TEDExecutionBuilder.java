/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 - 2020 Compuware Corporation
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
import org.kohsuke.stapler.StaplerRequest;

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
 * Sample {@link Builder}.
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
	
	private String repositoryName = DescriptorImpl.defaultRepositoryName;
	private String resultsRepositoryName = DescriptorImpl.defaultResultsRepositoryName;

	private String selectExecutionTypeRadio = DescriptorImpl.selectSingleSpecExecutionValue;
	
	private String specificationName = DescriptorImpl.defaultSpecificationName;
	private String specificationType; 
	private String specificationList = DescriptorImpl.defaultSpecificationList;
	
	private String executionContext = DescriptorImpl.defaultExecutionContext;
	private String executionTimeout = DescriptorImpl.defaultExecutionTimeout;
	private String exitOnFailure = DescriptorImpl.defaultExitOnFailure;

	//CES
	private boolean defineCES = DescriptorImpl.defaultDefineCES;
	private String cesURL;
	private boolean useCloudCES = DescriptorImpl.defaultUseCloud;
	private String cloudCustomerNo = DescriptorImpl.defaultCloudCustomerNo;
	private String cloudSiteID = DescriptorImpl.defaultCloudSiteID;

	//Communication Manager
	private String communicationManager = DescriptorImpl.defaultCommunicationManager;
	private String communicationManagerPort = DescriptorImpl.defaultCommunicationManagerPort;
	
	//Distributed - Execution Server
	private String executionServer = DescriptorImpl.defaultExecutionServer;
	private String executionServerPort = DescriptorImpl.defaultExecutionServerPort;

	//Mainframe
	/**Host Connection plugin*/	
	//Host, port, CCSID should come from this. 
	private String connectionId; 

	/** Host credentials plugin */
	//User ID, pwd should come from this.
	private String credentialsId; 
	
	//No support for keystore, and certificate via jenkins yet. 
	
	private String jclJobcardLine1 = DescriptorImpl.defaultJCLJobcardLine1;
	private String jclJobcardLine2 = DescriptorImpl.defaultJCLJobcardLine2;
	private String jclJobcardLine3 = DescriptorImpl.defaultJCLJobcardLine3;
	private String jclJobcardLine4 = DescriptorImpl.defaultJCLJobcardLine4;
	private String jclJobcardLine5 = DescriptorImpl.defaultJCLJobcardLine5;

	private String datasetHighLevelQualifier = DescriptorImpl.defaultDatasetHighLevelQualifier;
	private String temporaryDatasetPrefix = DescriptorImpl.defaultTemporaryDatasetPrefix;
	private String temporaryDatasetSuffix = DescriptorImpl.defaultTemporaryDatasetSuffix;

	private String dpOverrideFADEBUG = DescriptorImpl.defaultDpOverrideFADEBUG;
	private String dpOverrideFAEXPATH = DescriptorImpl.defaultDpOverrideFAEXPATH;
	private String dpOverrideFAIPADDR = DescriptorImpl.defaultDpOverrideFAJPADDR;
	private String dpOverrideFAJOPTS = DescriptorImpl.defaultDpOverrideFAJOPTS;
	private String dpOverrideFAJPATH = DescriptorImpl.defaultDpOverrideFAJPATH;
	
	/**
	 * Fields for Code Coverage.
	 */
	private boolean haltPipelineOnFailure = DescriptorImpl.defaultHaltPipelineOnFailure;
	public static final String haltPipelineTitle = "Halt pipeline if errors occur"; //NOSONAR
	
	@DataBoundConstructor
	public TEDExecutionBuilder(String repositoryName, String specificationName, 
			String specificationType, String specificationList, 
			String communicationManager, String communicationManagerPort, 
			String executionServer, String executionServerPort, boolean useCloudCES,  
			String cesURL, String customerNo, String siteId,
			String connectionId, String credentialsID) { 
		super();
		this.repositoryName = repositoryName;
		this.specificationName = specificationName;
		this.specificationType = specificationType;
		this.specificationList = specificationList;
		this.communicationManager = communicationManager;
		this.communicationManagerPort = communicationManagerPort;
		this.executionServer = executionServer;
		this.executionServerPort = executionServerPort;
		this.useCloudCES = useCloudCES;
		this.cesURL = cesURL;
		this.cloudCustomerNo = customerNo;
		this.cloudSiteID = siteId;
		this.connectionId = connectionId;
		this.credentialsId = credentialsID;
	}
	
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
		
		if(getConnectionId()!=null || !getConnectionId().isEmpty()) {
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();

			HostConnection connection = null;
			if (globalConfig != null) {
				connection = globalConfig.getHostConnection(getConnectionId());
			}
			if (connection == null) {
				throw new IllegalArgumentException("No host connection defined. Check project and global configurations to make sure a valid host connection is set."); //$NON-NLS-1$
			}
			
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

	public String getRepositoryName() {
		return repositoryName;
	}

	@DataBoundSetter
	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getResultsRepositoryName() {
		return resultsRepositoryName;
	}

	@DataBoundSetter
	public void setResultsRepositoryName(String resultsRepositoryName) {
		this.resultsRepositoryName = resultsRepositoryName;
	}

	public String getSelectExecutionTypeRadio() {
		return selectExecutionTypeRadio;
	}

	@DataBoundSetter
	public void setSelectExecutionTypeRadio(String selectExecutionTypeRadio) {
		this.selectExecutionTypeRadio = selectExecutionTypeRadio;
	}

	public String getSpecificationName() {
		return specificationName;
	}

	@DataBoundSetter
	public void setSpecificationName(String specificationName) {
		this.specificationName = specificationName;
	}

	public String getSpecificationType() {
		return specificationType;
	}

	@DataBoundSetter
	public void setSpecificationType(String specificationType) {
		this.specificationType = specificationType;
	}

	public String getSpecificationList() {
		return specificationList;
	}

	@DataBoundSetter
	public void setSpecificationList(String specificationList) {
		this.specificationList = specificationList;
	}

	public String getExecutionContext() {
		return executionContext;
	}

	@DataBoundSetter
	public void setExecutionContext(String executionContext) {
		this.executionContext = executionContext;
	}
	
	public String getExitOnFailure() {
		return exitOnFailure;
	}

	@DataBoundSetter
	public void setExitOnFailure(String exitOnFailure) {
		this.exitOnFailure = exitOnFailure;
	}

	public String getExecutionTimeout() {
		return executionTimeout;
	}

	@DataBoundSetter
	public void setExecutionTimeout(String executionTimeout) {
		this.executionTimeout = executionTimeout;
	}

	public boolean getDefineCES() {
		return defineCES;
	}

	@DataBoundSetter
	public void setDefineCES(boolean defineCES) {
		this.defineCES = defineCES;
	}

	public boolean getUseCloudCES() {
		return useCloudCES;
	}

	@DataBoundSetter
	public void setUseCloudCES(boolean useCloudCES) {
		this.useCloudCES = useCloudCES;
	}

	public String getCesURL() {
		return cesURL;
	}

	@DataBoundSetter
	public void setCesURL(String cesURL) {
		this.cesURL = cesURL;
	}

	public String getCloudCustomerNo() {
		return cloudCustomerNo;
	}

	@DataBoundSetter
	public void setCloudCustomerNo(String cloudCustomerNo) {
		this.cloudCustomerNo = cloudCustomerNo;
	}

	public String getCloudSiteID() {
		return cloudSiteID;
	}

	@DataBoundSetter
	public void setCloudSiteID(String cloudSiteID) {
		this.cloudSiteID = cloudSiteID;
	}

	public String getCommunicationManager() {
		return communicationManager;
	}

	@DataBoundSetter
	public void setCommunicationManager(String communicationManager) {
		this.communicationManager = communicationManager;
	}

	public String getCommunicationManagerPort() {
		return communicationManagerPort;
	}

	@DataBoundSetter
	public void setCommunicationManagerPort(String communicationManagerPort) {
		this.communicationManagerPort = communicationManagerPort;
	}

	public String getExecutionServer() {
		return executionServer;
	}

	@DataBoundSetter
	public void setExecutionServer(String executionServer) {
		this.executionServer = executionServer;
	}

	public String getExecutionServerPort() {
		return executionServerPort;
	}

	@DataBoundSetter
	public void setExecutionServerPort(String executionServerPort) {
		this.executionServerPort = executionServerPort;
	}

	public String getConnectionId() {
		return connectionId;
	}

	@DataBoundSetter
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}

	public String getCredentialsId() {
		return credentialsId;
	}

	@DataBoundSetter
	public void setCredentialsId(String credentialsId) {
		this.credentialsId = credentialsId;
	}

	public String getJCLJobcardLine1() {
		return jclJobcardLine1;
	}

	@DataBoundSetter
	public void setJCLJobcardLine1(String jclJobcardLine1) {
		this.jclJobcardLine1 = jclJobcardLine1;
	}

	public String getJCLJobcardLine2() {
		return jclJobcardLine2;
	}

	@DataBoundSetter
	public void setJCLJobcardLine2(String jclJobcardLine2) {
		this.jclJobcardLine2 = jclJobcardLine2;
	}

	public String getJCLJobcardLine3() {
		return jclJobcardLine3;
	}

	@DataBoundSetter
	public void setJCLJobcardLine3(String jclJobcardLine3) {
		this.jclJobcardLine3 = jclJobcardLine3;
	}

	public String getJCLJobcardLine4() {
		return jclJobcardLine4;
	}

	@DataBoundSetter
	public void setJCLJobcardLine4(String jclJobcardLine4) {
		this.jclJobcardLine4 = jclJobcardLine4;
	}

	@DataBoundSetter
	public String getJCLJobcardLine5() {
		return jclJobcardLine5;
	}

	@DataBoundSetter
	public void setJCLJobcardLine5(String jclJobcardLine5) {
		this.jclJobcardLine5 = jclJobcardLine5;
	}

	public String getDatasetHighLevelQualifier() {
		return datasetHighLevelQualifier;
	}

	@DataBoundSetter
	public void setDatasetHighLevelQualifier(String datasetHighLevelQualifier) {
		this.datasetHighLevelQualifier = datasetHighLevelQualifier;
	}

	public String getTemporaryDatasetPrefix() {
		return temporaryDatasetPrefix;
	}

	@DataBoundSetter
	public void setTemporaryDatasetPrefix(String temporaryDatasetPrefix) {
		this.temporaryDatasetPrefix = temporaryDatasetPrefix;
	}

	public String getTemporaryDatasetSuffix() {
		return temporaryDatasetSuffix;
	}

	@DataBoundSetter
	public void setTemporaryDatasetSuffix(String temporaryDatasetSuffix) {
		this.temporaryDatasetSuffix = temporaryDatasetSuffix;
	}

	public String getDPOverrideFADEBUG() {
		return dpOverrideFADEBUG;
	}

	@DataBoundSetter
	public void setDPOverrideFADEBUG(String dpOverrideFADEBUG) {
		this.dpOverrideFADEBUG = dpOverrideFADEBUG;
	}

	public String getDPOverrideFAEXPATH() {
		return dpOverrideFAEXPATH;
	}

	@DataBoundSetter
	public void setDPOverrideFAEXPATH(String dpOverrideFAEXPATH) {
		this.dpOverrideFAEXPATH = dpOverrideFAEXPATH;
	}

	public String getDPOverrideFAIPADDR() {
		return dpOverrideFAIPADDR;
	}

	@DataBoundSetter
	public void setDPOverrideFAIPADDR(String dpOverrideFAIPADDR) {
		this.dpOverrideFAIPADDR = dpOverrideFAIPADDR;
	}

	public String getDPOverrideFAJOPTS() {
		return dpOverrideFAJOPTS;
	}

	@DataBoundSetter
	public void setDPOverrideFAJOPTS(String dpOverrideFAJOPTS) {
		this.dpOverrideFAJOPTS = dpOverrideFAJOPTS;
	}

	public String getDPOverrideFAJPATH() {
		return dpOverrideFAJPATH;
	}

	@DataBoundSetter
	public void setDPOverrideFAJPATH(String dpOverrideFAJPATH) {
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
	 * Return the associated text for the "Halt pipeline if errors occur" field's title
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
		public static final String defaultSpecificationType = "";
		
		public static final String defaultSpecificationList = "";
		
		public static final String defaultExecutionContext = "";
		public static final String defaultExecutionTimeout = "";
		public static final String defaultExitOnFailure = "";

		public static final Boolean defaultDefineCES = false; 
		public static final Boolean defaultUseCloud = false;
		public static final String defaultCloudCustomerNo = "";
		public static final String defaultCloudSiteID = "";
		
		public static final String defaultCommunicationManager = "";
		public static final String defaultCommunicationManagerPort = "";
		
		public static final String defaultExecutionServer = "";
		public static final String defaultExecutionServerPort = "";
		
		public static final String defaultHostConnectionId = "";
		public static final String defaultHostCredentialId = "";
		
		public static final String defaultJCLJobcardLine1 = "";
		public static final String defaultJCLJobcardLine2 = "";
		public static final String defaultJCLJobcardLine3 = "";
		public static final String defaultJCLJobcardLine4 = "";
		public static final String defaultJCLJobcardLine5 = "";

		public static final String defaultDatasetHighLevelQualifier = "";
		public static final String defaultTemporaryDatasetPrefix = "";
		public static final String defaultTemporaryDatasetSuffix = "";

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
		
		public ListBoxModel doFillSpecificationTypeItems(@AncestorInPath Jenkins context, @QueryParameter String specType, @AncestorInPath Item project) {
			ListBoxModel specTypeModel = new ListBoxModel();
			specTypeModel.add(new Option(SpecificationType.COMPARE.getDisplayName(), SpecificationType.COMPARE.getValue(), (specType == null || !SpecificationType.COMPARE.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.CONVERT.getDisplayName(), SpecificationType.CONVERT.getValue(), (specType == null || !SpecificationType.CONVERT.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.EXSUITE.getDisplayName(), SpecificationType.EXSUITE.getValue(), (specType == null || !SpecificationType.EXSUITE.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.EXTRACT.getDisplayName(), SpecificationType.EXTRACT.getValue(), (specType == null || !SpecificationType.EXTRACT.getValue().equals(specType)? false : true))); //NOSONAR
			specTypeModel.add(new Option(SpecificationType.LOAD.getDisplayName(), SpecificationType.LOAD.getValue(), (specType == null || !SpecificationType.LOAD.getValue().equals(specType)? false : true))); //NOSONAR

			return specTypeModel;
		}
		
		public ListBoxModel doFillExitOnFailureItems(@AncestorInPath Jenkins context, @QueryParameter String exitOnFailure, @AncestorInPath Item project) {
			ListBoxModel exitOnFailureOptionsModel = new ListBoxModel();
			exitOnFailureOptionsModel.add(new Option(BooleanResponse.EMPTY.getDisplayName(), BooleanResponse.EMPTY.getValue(), (exitOnFailure == null))); //NOSONAR
			exitOnFailureOptionsModel.add(new Option(BooleanResponse.TRUE.getDisplayName(), BooleanResponse.TRUE.getValue(), (exitOnFailure != null && !BooleanResponse.TRUE.getValue().equals(exitOnFailure)? false : true))); //NOSONAR
			exitOnFailureOptionsModel.add(new Option(BooleanResponse.FALSE.getDisplayName(), BooleanResponse.FALSE.getValue(), (exitOnFailure != null && !BooleanResponse.FALSE.getValue().equals(exitOnFailure)? false : true))); //NOSONAR

			return exitOnFailureOptionsModel;
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
		 * @param serverUrl
		 *            The serverUrl id for the user.
		 * 
		 * @return serverUrl selections
		 * 
		 */
		public ListBoxModel doFillServerUrlItems(@QueryParameter String serverUrl)
		{

			ListBoxModel model = new ListBoxModel();
			model.add(new Option("", "", false)); //$NON-NLS-1$ //$NON-NLS-2$
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
			if (globalConfig != null)
			{
				HostConnection[] hostConnections = globalConfig.getHostConnections();

				for (HostConnection connection : hostConnections)
				{
					String cesServerURL = connection.getCesUrl();
					if (cesServerURL != null && !cesServerURL.isEmpty())
					{
						boolean isSelected = false;
						if (serverUrl != null)
						{
							isSelected = serverUrl.equalsIgnoreCase(cesServerURL);
						}

						String cesValue = cesServerURL;
						Option opt = new Option(cesValue, cesServerURL, isSelected);
						boolean exists = false;
						
						ListIterator<Option> li = model.listIterator();
						while (li.hasNext())
						{
							if (li.next().value.equalsIgnoreCase(cesServerURL))
							{
								exists = true;
								break;
							}
						}
						
						if (!exists)
						{
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
