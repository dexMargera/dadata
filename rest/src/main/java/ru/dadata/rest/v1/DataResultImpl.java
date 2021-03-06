package ru.dadata.rest.v1;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import ru.dadata.rest.api.DataResult;
import ru.dadata.rest.api.DataResultCode;
import ru.dadata.rest.api.Entity;
import ru.dadata.rest.api.QueryFieldType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class DataResultImpl implements DataResult {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final DataResultCode resultCode;
    private final String result;
    private final String errorDetail;

    DataResultImpl(HttpResponse response) {
        this.resultCode = DataResultCode.valueOf(response.getStatusLine().getStatusCode());
        this.result = getResultSource(response.getEntity());
        this.errorDetail = parseResult().detail;

    }

    private DataResponse parseResult() {
        try {
            DataResponse response = GSON.fromJson(result, DataResponse.class);
            fields = new QueryFieldType[response.structure.length];
            int i = 0;
            for (String fieldName : response.structure) {
                fields[i++] = QueryFieldType.valueOf(fieldName);
            }
            for (EntityImpl1v[] value : response.data) {
                for (int k = 0; k < fields.length; k++) {
                    value[k].setEntityType(fields[k]);
                }
                entities.add(value);
            }
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new DataResponse();
        }
    }

    private String getResultSource(HttpEntity entity) {
        try {
            return IOUtils.toString(entity.getContent(), "UTF-8");
        } catch (IOException e) {
            return null;
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

    @Override
    public DataResultCode getResultCode() {
        return resultCode;
    }

    @Override
    public String getErrorDetail() {
        return errorDetail;
    }

    @Override
    public String getSource() {
        return result;
    }


    private QueryFieldType[] fields = new QueryFieldType[0];

    @Override
    public QueryFieldType[] getStructure() {
        return fields;
    }

    private final List<Entity[]> entities = new ArrayList<Entity[]>();

    @Override
    public List<Entity[]> getData() {
        return entities;
    }
}
