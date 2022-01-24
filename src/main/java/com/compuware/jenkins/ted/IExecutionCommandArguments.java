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

public interface IExecutionCommandArguments  {
	
	String[] COMMAND = new String[] {"-cmd", "-cmd"};
	String[] REPOSITORY = new String[] {"-repository", "-r"};
	String[] RESULTS_REPOSITORY = new String[] {"-results-repository", "-rr"};
	
	String[] SPECIFICATION = new String[] {"-specification", "-s"};
	String[] SPECIFICATION_TYPE = new String[] {"-specification-type", "-st"};
	String[] SPECIFICATION_LIST = new String[] {"-specification-list", "-sl"};
	
	String[] EXIT_ON_FAILURE = new String[] {"-exit-on-failure", "-eof"};
	String[] EXECUTION_TIMEOUT = new String[] {"-execution-timeout", "-t"};
	String[] EXECUTION_CONTEXT = new String[] {"-execution-context", "-ec"}; 
													
	String[] COMM_MANAGER = new String[] {"-comm-manager", "-cm"};
	String[] COMM_MANAGER_PORT = new String[] {"-comm-manager-port", "-cmp"};
	
	String[] CES_URL = new String[] {"-ces-uri", "-ces"};
	String[] USE_CLOUD_CES = new String[] {"-use-cloud", "-ucd"};
	String[] CES_CUSTOMER_NUMBER = new String[] {"-ces-cust-no", "-cno"};
	String[] CES_SITE_ID = new String[] {"-ces-site-id", "-sid"};
	
	String[] EXECUTION_SERVER = new String[] {"-execution-server", "-es"};
	String[] EXECUTION_SERVER_PORT = new String[] {"-execution-server-port", "-esp"};
	
	String[] EXECUTION_HOST = new String[] {"-execution-host", "-eh"};
	String[] EXECUTION_HOST_PORT = new String[] {"-execution-host-port", "-ehp"};
	String[] CCSID = new String[] {"-ccsid", "-ccs"};

	String[] HCI_USER_ID = new String[] {"-hci-userid", "-hid"};
	String[] HCI_PASSWORD = new String[] {"-hci-password", "-hpw"};

	String[] HCI_CERTIFICATE = new String[] {"-certificate", "-certificate"};
	
	String[] HCI_KEYSTORE = new String[] {"-keystore", "-keystore"};
	String[] HCI_CERTIFICATE_ALIAS = new String[] {"-certificateAlias ", "-certificateAlias"};
	String[] HCI_KEYSTORE_PASSWORD = new String[] {"-keystorePassword", "-keystorePassword"};
	
	String[] JCL_JOBCARD1 = new String[] {"-jcl-jobcard1", "-j1"};
	String[] JCL_JOBCARD2 = new String[] {"-jcl-jobcard2", "-j2"};
	String[] JCL_JOBCARD3 = new String[] {"-jcl-jobcard3", "-j3"};
	String[] JCL_JOBCARD4 = new String[] {"-jcl-jobcard4", "-j4"};
	String[] JCL_JOBCARD5 = new String[] {"-jcl-jobcard5", "-j5"};
	
	String[] DATASET_HLQ = new String[] {"-dataset-hlq", "-hlq"};
	
	String[] TEMP_DATASET_PREFIX = new String[] {"-temp-dataset-prefix", "-px"};
	String[] TEMP_DATASET_SUFFIX = new String[] {"-temp-dataset-suffix", "-sx"};
	
	String[] FADEBUG = new String[] {"-fadebug", "-fdb"};
	String[] FAEXPATH = new String[] {"-faexpath", "-fxp"};
	String[] FAIPADDR = new String[] {"-faipaddr", "-fip"};
	String[] FAJOPTS = new String[] {"-fajopts", "-fjo"};
	String[] FAJPATH = new String[] {"-fajpath", "-fjp"};

}