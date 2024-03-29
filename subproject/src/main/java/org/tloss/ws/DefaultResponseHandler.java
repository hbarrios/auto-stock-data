package org.tloss.ws;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.DeflateDecompressingEntity;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.util.EntityUtils;

public class DefaultResponseHandler implements ResponseHandler<String> {

	public String handleResponse(final HttpResponse response)
			throws HttpResponseException, IOException {
		HttpEntity entity = response.getEntity();
		if (entity.getContentEncoding() != null
				&& "deflate".equals(entity.getContentEncoding().getValue())) {
			entity = new DeflateDecompressingEntity(entity);
		}
		if (entity.getContentEncoding() != null
				&& "gzip".equals(entity.getContentEncoding().getValue())) {
			entity = new GzipDecompressingEntity(entity);
		}
		Header[] headers = response.getAllHeaders();
		for(int i=0;i<headers.length;i++){
			System.out.println(headers[i]);
		}
		return entity == null ? null : EntityUtils.toString(entity);
	}
}
