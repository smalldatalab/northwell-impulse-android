package edu.cornell.tech.foundry;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.researchstack.backbone.StorageAccess;
import org.researchstack.skin.ResourceManager;
import org.smalldatalab.northwell.impulse.R;
import org.smalldatalab.northwell.impulse.SampleResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import edu.cornell.tech.foundry.DefaultStepGenerators.descriptors.CustomStepDescriptor;

/**
 * Created by jameskizer on 12/8/16.
 */
public class CTFStepBuilderHelper implements CTFStateHelper {

    private Context context;
    private ResourceManager resourceManager;
    private Gson gson;
    private JsonParser jsonParser;
    private String pathName;

    CTFStepBuilderHelper(Context context, ResourceManager resourceManager) {
        super();
        this.context = context;
        this.resourceManager = resourceManager;
        this.gson = new Gson();
        this.jsonParser = new JsonParser();
        this.pathName = context.getString(R.string.ctf_state_helper_path);
    }

    public Context getContext() {
        return this.context;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public Gson getGson() {
        return this.gson;
    }

    //utilities
    @Nullable
    public JsonElement getJsonElementForFilename(String filename) {
        String jsonPath = this.resourceManager.generatePath(SampleResourceManager.SURVEY, filename);
        InputStream stream = this.resourceManager.getResouceAsInputStream(this.context, jsonPath);
        Reader reader = null;
        try
        {
            reader = new InputStreamReader(stream, "UTF-8");
        }
        catch(UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        JsonElement element = null;
        try {
            element = this.jsonParser.parse(reader);
        }
        catch(Exception e) {
            Log.w(this.getClass().getSimpleName(), "could not parse json element", e);
            return null;
        }
        return element;
    }

    @Nullable
    public CustomStepDescriptor getCustomStepDescriptor(JsonObject jsonObject) {
        CustomStepDescriptor stepDescriptor = this.getGson().fromJson(jsonObject, CustomStepDescriptor.class);
        if (stepDescriptor.parameters == null &&
                stepDescriptor.parameterFileName != null &&
                !stepDescriptor.parameterFileName.isEmpty()) {
            JsonElement element = this.getJsonElementForFilename(stepDescriptor.parameterFileName);
            if (element.isJsonObject()) {
                stepDescriptor.parameters = element.getAsJsonObject();
            }
        }
        return stepDescriptor;
    }

    public CTFStateHelper getStateHelper() {
        return this;
    }

    //state helper methods
    public String valueInState(String key) {
        StringBuilder pathBuilder = new StringBuilder(this.pathName);
        pathBuilder.append('/');
        pathBuilder.append(key);

        return new String(StorageAccess.getInstance().getFileAccess().readData(context, pathBuilder.toString()));
    }

    public void setValueInState(String key, String value) {

        StringBuilder pathBuilder = new StringBuilder(this.pathName);
        pathBuilder.append('/');
        pathBuilder.append(key);

        StorageAccess
                .getInstance()
                .getFileAccess()
                .writeData(
                        this.getContext(),
                        pathBuilder.toString(),
                        value.getBytes()
        );
    }

//    StorageAccess.getInstance()
//            .getFileAccess()
//    .writeData(context, userSessionPath, userSessionJson.getBytes());
//

}
