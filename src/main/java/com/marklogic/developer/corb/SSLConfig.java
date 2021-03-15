/*
 * Copyright (c) 2004-2021 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 */
package com.marklogic.developer.corb;

import com.marklogic.xcc.SecurityOptions;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.net.ssl.SSLContext;

/**
 *
 * @since 2.2.0
 */
public interface SSLConfig {
	void setProperties(Properties props);
	void setDecrypter(Decrypter decrypter);
	String[] getEnabledCipherSuites();
	String[] getEnabledProtocols();
	SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException;
	SecurityOptions getSecurityOptions() throws NoSuchAlgorithmException, KeyManagementException;
}
