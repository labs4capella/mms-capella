/*******************************************************************************
 * Copyright (c) 2020 Thales Global Services S.A.S.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package com.thalesgroup.mde.openmbee.connector.mms.utils;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.ContentResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestApiHelper {
	private final String baseUrl;
	private String ticket;
	private String autData;
	
	public RestApiHelper(String baseUrl, String ticket, String autData) {
		this.baseUrl = baseUrl;
		//this.ticket = ticket;
		this.autData = autData;
	}
	
	/**
	 * Update the authentication data.
	 * 
	 * @param autData the new authentication data
	 */
	public void setAutData(String autData) {
		this.autData = autData;
	}
	
	/**
	 * Update communication ticket.
	 * 
	 * @param ticket the new ticket for communication.
	 */
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	
	public Request prepareGet(String urlPostfix, Object... params) {
		String postfix = preparePostfix(urlPostfix, params);
		Request get = Request.Get(baseUrl+postfix);
		return addAuthHeader(get);
	}
	
	public Request preparePost(String urlPostfix, Object... params) {
		String postfix = preparePostfix(urlPostfix, params);
		Request get = Request.Post(baseUrl+postfix);
		return addAuthHeader(get);
	}
	
	public DeleteRequestWithBody prepareDelete(String urlPostfix, Object... params) {
		String postfix = preparePostfix(urlPostfix, params);
		DeleteRequestWithBody get = DeleteRequestWithBody.Delete(baseUrl+postfix);
		return addAuthHeader(get);
	}
	
	/**
	 * Mimic the Fluent {@link Request} API of Apache Http Client because the delete of it 
	 * cannot handle body content but we need it when try to delete model elements.
	 */
	public static class DeleteRequestWithBody {
		private final HttpDeleteWithBody request;
		
		private static class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
			public String getMethod() {
				return HttpDelete.METHOD_NAME;
			}
		 
			HttpDeleteWithBody(final String uri) {
				super();
				setURI(URI.create(uri));
			}
		}
		
		/**
		 * The Fluent {@link org.apache.http.client.fluent.Response} API of Apache Http Client
		 * needs to be mimicked because the original has no public constructor or any API to create
		 * it but the response of {@link DeleteRequestWithBody} also need to have the same API.
		 */
		public static class DeleteResponse {
			private final HttpResponse response;
			private boolean consumed;
			
			DeleteResponse(HttpResponse response) {
				this.response = response;
				this.consumed = false;
			}
			
			private void assertNotConsumed() {
				if (this.consumed) {
					throw new IllegalStateException("Response content has been already consumed"); //$NON-NLS-1$
				}
			}
			
			<T> T handleResponse(final ResponseHandler<T> handler) throws ClientProtocolException, IOException {
				assertNotConsumed();
				return handler.handleResponse(this.response);
			}

			public Content returnContent() throws ClientProtocolException, IOException {
				return handleResponse(new ContentResponseHandler());
			}
			
			public HttpResponse returnResponse() throws IOException {
				assertNotConsumed();
				try {
					final HttpEntity entity = this.response.getEntity();
					if (entity != null) {
						final ByteArrayEntity byteArrayEntity = new ByteArrayEntity(
								EntityUtils.toByteArray(entity));
						final ContentType contentType = ContentType.getOrDefault(entity);
						byteArrayEntity.setContentType(contentType.toString());
						this.response.setEntity(byteArrayEntity);
					}
					return this.response;
				} finally {
					this.consumed = true;
				}
			}
		}
		
		public static DeleteRequestWithBody Delete(String url) {
			return new DeleteRequestWithBody(url);
		}
		
		private DeleteRequestWithBody(String url) {
			request = new HttpDeleteWithBody(url);
		}
		
		public DeleteRequestWithBody bodyString(String content, ContentType contentType) {
			request.setEntity(new StringEntity(content, contentType));
			return this;
		}
		
		public DeleteRequestWithBody addHeader(String name, String value) {
			request.addHeader(name, value);
			return this;
		}
		
		public DeleteResponse execute() throws IOException {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			return new DeleteResponse(httpclient.execute(request));
			
		}
		
		@Override
		public String toString() {
			return this.request.getRequestLine().toString();
		}
	}

	private String preparePostfix(String urlPostfix, Object... params) {
		String postfix = ""; //$NON-NLS-1$
		if(urlPostfix != null && urlPostfix.length()>0) {
			if(!baseUrl.endsWith("/")) { //$NON-NLS-1$
				urlPostfix = "/"+urlPostfix; //$NON-NLS-1$
			}
			if(params != null && params.length > 0) {
				postfix = String.format(urlPostfix, params);
			} else {
				postfix = urlPostfix;
			}
		}
		return ticket == null ? postfix : (postfix+(postfix.contains("?") ? "&" : "?")+"alf_ticket="+ticket); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}
	
	private DeleteRequestWithBody addAuthHeader(DeleteRequestWithBody req) {
		return ticket == null ? req.addHeader("Authorization", autData) : req; //$NON-NLS-1$
	}
	
	private Request addAuthHeader(Request req) {
		return ticket == null ? req.addHeader("Authorization", autData) : req; //$NON-NLS-1$
	}
}
