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

public final class ExecutionCommandArguments {

	/**
	 * Private constructor to hide the protected one.
	 */
	private ExecutionCommandArguments() {

	}

	/**
	 * Returns COMMAND arguments.
	 * 
	 * @return COMMAND arguments.
	 */
	protected static String[] getCOMMAND() {
		return new String[] { "-cmd", "-cmd" };
	}

	/**
	 * Returns REPOSITORY arguments.
	 * 
	 * @return REPOSITORY arguments.
	 */
	protected static String[] getREPOSITORY() {
		return new String[] { "-repository", "-r" };
	}

	/**
	 * Returns RESULTS_REPOSITORY arguments.
	 * 
	 * @return RESULTS_REPOSITORY arguments.
	 */
	protected static String[] getResultsRepository() {
		return new String[] { "-results-repository", "-rr" };
	}

	/**
	 * Returns SPECIFICATION arguments.
	 * 
	 * @return SPECIFICATION arguments.
	 */
	protected static String[] getSPECIFICATION() {
		return new String[] { "-specification", "-s" };
	}

	/**
	 * Returns SPECIFICATION_TYPE arguments.
	 * 
	 * @return SPECIFICATION_TYPE arguments.
	 */
	protected static String[] getSpecificationType() {
		return new String[] { "-specification-type", "-st" };
	}

	/**
	 * Returns SPECIFICATION_LIST arguments.
	 * 
	 * @return SPECIFICATION_LIST arguments.
	 */
	protected static String[] getSpecificationList() {
		return new String[] { "-specification-list", "-sl" };
	}

	/**
	 * Returns EXIT_ON_FAILURE arguments.
	 * 
	 * @return EXIT_ON_FAILURE arguments.
	 */
	protected static String[] getExitOnFailure() {
		return new String[] { "-exit-on-failure", "-eof" };
	}

	/**
	 * Returns EXECUTION_TIMEOUT arguments.
	 * 
	 * @return EXECUTION_TIMEOUT arguments.
	 */
	protected static String[] getExecutionTimeout() {
		return new String[] { "-execution-timeout", "-t" };
	}

	/**
	 * Returns EXECUTION_CONTEXT arguments.
	 * 
	 * @return EXECUTION_CONTEXT arguments.
	 */
	protected static String[] getExecutionContext() {
		return new String[] { "-execution-context", "-ec" };
	}

	/**
	 * Returns COMM_MANAGER arguments.
	 * 
	 * @return COMM_MANAGER arguments.
	 */
	protected static String[] getCommManager() {
		return new String[] { "-comm-manager", "-cm" };
	}

	/**
	 * Returns COMM_MANAGER_PORT arguments.
	 * 
	 * @return COMM_MANAGER_PORT arguments.
	 */
	protected static String[] getCommManagerPort() {
		return new String[] { "-comm-manager-port", "-cmp" };
	}

	/**
	 * Returns CES_URL arguments.
	 * 
	 * @return CES_URL arguments.
	 */
	protected static String[] getCESURL() {
		return new String[] { "-ces-uri", "-ces" };
	}

	/**
	 * Returns USE_CLOUD_CES arguments.
	 * 
	 * @return USE_CLOUD_CES arguments.
	 */
	protected static String[] getUseCloudCES() {
		return new String[] { "-use-cloud", "-ucd" };
	}

	/**
	 * Returns CES_CUSTOMER_NUMBER arguments.
	 * 
	 * @return CES_CUSTOMER_NUMBER arguments.
	 */
	protected static String[] getCESCustomerNumber() {
		return new String[] { "-ces-cust-no", "-cno" };
	}

	/**
	 * Returns CES_SITE_ID arguments.
	 * 
	 * @return CES_SITE_ID arguments.
	 */
	protected static String[] getCESSiteID() {
		return new String[] { "-ces-site-id", "-sid" };
	}

	/**
	 * Returns EXECUTION_SERVER arguments.
	 * 
	 * @return EXECUTION_SERVER arguments.
	 */
	protected static String[] getExecutionServer() {
		return new String[] { "-execution-server", "-es" };
	}

	/**
	 * Returns EXECUTION_SERVER_PORT arguments.
	 * 
	 * @return EXECUTION_SERVER_PORT arguments.
	 */
	protected static String[] getExecutionServerPort() {
		return new String[] { "-execution-server-port", "-esp" };
	}

	/**
	 * Returns EXECUTION_HOST arguments.
	 * 
	 * @return EXECUTION_HOST arguments.
	 */
	protected static String[] getExecutionHost() {
		return new String[] { "-execution-host", "-eh" };
	}

	/**
	 * Returns EXECUTION_HOST_PORT arguments.
	 * 
	 * @return EXECUTION_HOST_PORT arguments.
	 */
	protected static String[] getExecutionHostPort() {
		return new String[] { "-execution-host-port", "-ehp" };
	}

	/**
	 * Returns CCSID arguments.
	 * 
	 * @return CCSID arguments.
	 */
	protected static String[] getCCSID() {
		return new String[] { "-ccsid", "-ccs" };
	}

	/**
	 * Returns EXIT_ON_FAILURE arguments.
	 * 
	 * @return EXIT_ON_FAILURE arguments.
	 */
	protected static String[] getHCIUserID() {
		return new String[] { "-hci-userid", "-hid" };
	}

	/**
	 * Returns HCI_PASSWORD arguments.
	 * 
	 * @return HCI_PASSWORD arguments.
	 */
	protected static String[] getHCIPassword() {
		return new String[] { "-hci-password", "-hpw" };
	}

	/**
	 * Returns HCI_CERTIFICATE arguments.
	 * 
	 * @return HCI_CERTIFICATE arguments.
	 */
	protected static String[] getHCICertificate() {
		return new String[] { "-certificate", "-certificate" };
	}

	/**
	 * Returns HCI_KEYSTORE arguments.
	 * 
	 * @return HCI_KEYSTORE arguments.
	 */
	protected static String[] getHCIKeystore() {
		return new String[] { "-keystore", "-keystore" };
	}

	/**
	 * Returns HCI_CERTIFICATE_ALIAS arguments.
	 * 
	 * @return HCI_CERTIFICATE_ALIAS arguments.
	 */
	protected static String[] getHCICertificateAlias() {
		return new String[] { "-certificateAlias ", "-certificateAlias" };
	}

	/**
	 * Returns HCI_KEYSTORE_PASSWORD arguments.
	 * 
	 * @return HCI_KEYSTORE_PASSWORD arguments.
	 */
	protected static String[] getHCIKeystorePassword() {
		return new String[] { "-keystorePassword", "-keystorePassword" };
	}

	/**
	 * Returns JCL_JOBCARD1 arguments.
	 * 
	 * @return JCL_JOBCARD1 arguments.
	 */
	protected static String[] getJCLJobcard1() {
		return new String[] { "-jcl-jobcard1", "-j1" };
	}

	/**
	 * Returns JCL_JOBCARD2 arguments.
	 * 
	 * @return JCL_JOBCARD2 arguments.
	 */
	protected static String[] getJCLJobcard2() {
		return new String[] { "-jcl-jobcard2", "-j2" };
	}

	/**
	 * Returns JCL_JOBCARD3 arguments.
	 * 
	 * @return JCL_JOBCARD3 arguments.
	 */
	protected static String[] getJCLJobcard3() {
		return new String[] { "-jcl-jobcard3", "-j3" };
	}

	/**
	 * Returns JCL_JOBCARD4 arguments.
	 * 
	 * @return JCL_JOBCARD4 arguments.
	 */
	protected static String[] getJCLJobcard4() {
		return new String[] { "-jcl-jobcard4", "-j4" };
	}

	/**
	 * Returns JCL_JOBCARD5 arguments.
	 * 
	 * @return JCL_JOBCARD5 arguments.
	 */
	protected static String[] getJCLJobcard5() {
		return new String[] { "-jcl-jobcard5", "-j5" };
	}

	/**
	 * Returns DATASET_HLQ arguments.
	 * 
	 * @return DATASET_HLQ arguments.
	 */
	protected static String[] getDatasetHLQ() {
		return new String[] { "-dataset-hlq", "-hlq" };
	}

	/**
	 * Returns TEMP_DATASET_PREFIX arguments.
	 * 
	 * @return TEMP_DATASET_PREFIX arguments.
	 */
	protected static String[] getTempDatasetPrefix() {
		return new String[] { "-temp-dataset-prefix", "-px" };
	}

	/**
	 * Returns TEMP_DATASET_SUFFIX arguments.
	 * 
	 * @return TEMP_DATASET_SUFFIX arguments.
	 */
	protected static String[] getTempDatasetSuffix() {
		return new String[] { "-temp-dataset-suffix", "-sx" };
	}

	/**
	 * Returns FADEBUG arguments.
	 * 
	 * @return FADEBUG arguments.
	 */
	protected static String[] getFADEBUG() {
		return new String[] { "-fadebug", "-fdb" };
	}

	/**
	 * Returns FAEXPATH arguments.
	 * 
	 * @return FAEXPATH arguments.
	 */
	protected static String[] getFAEXPATH() {
		return new String[] { "-faexpath", "-fxp" };
	}

	/**
	 * Returns FAIPADDR arguments.
	 * 
	 * @return FAIPADDR arguments.
	 */
	protected static String[] getFAIPADDR() {
		return new String[] { "-faipaddr", "-fip" };
	}

	/**
	 * Returns FAJOPTS arguments.
	 * 
	 * @return FAJOPTS arguments.
	 */
	protected static String[] getFAJOPTS() {
		return new String[] { "-fajopts", "-fjo" };
	}

	/**
	 * Returns FAJPATH arguments.
	 * 
	 * @return FAJPATH arguments.
	 */
	protected static String[] getFAJPATH() {
		return new String[] { "-fajpath", "-fjp" };
	}
}