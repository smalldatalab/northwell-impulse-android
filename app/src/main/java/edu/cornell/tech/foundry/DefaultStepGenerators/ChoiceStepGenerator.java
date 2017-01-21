package edu.cornell.tech.foundry.DefaultStepGenerators;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.smalldatalab.northwell.impulse.SDL.CTFHelpers;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.tech.foundry.CTFStepBuilderHelper;
import edu.cornell.tech.foundry.DefaultStepGenerators.descriptors.ChoiceStepDescriptor;
import edu.cornell.tech.foundry.DefaultStepGenerators.descriptors.ChoiceStepItemDescriptor;

/**
 * Created by jameskizer on 12/7/16.
 */
public abstract class ChoiceStepGenerator extends QuestionStepGenerator {

    public interface ChoiceFilter {
        boolean filter(ChoiceStepItemDescriptor itemDescriptor);
    }

    protected abstract boolean allowsMultiple();

    //default filter lets everything pass
    public ChoiceFilter generateFilter(CTFStepBuilderHelper helper, String type, JsonObject jsonObject) {
        return new ChoiceFilter() {
            @Override
            public boolean filter(ChoiceStepItemDescriptor itemDescriptor) {
                return true;
            }
        };

    }

    protected Choice[] generateChoices(List<ChoiceStepItemDescriptor> items, boolean shuffleItems)
    {
        Choice[] choices = new Choice[items.size()];

        List<ChoiceStepItemDescriptor> choiceItems = shuffleItems ? CTFHelpers.shuffled(items) : items;

        for(int i = 0; i < choiceItems.size(); i++)
        {
            ChoiceStepItemDescriptor choice = choiceItems.get(i);
            if(choice.value instanceof String)
            {
                choices[i] = new Choice<>(choice.prompt, (String) choice.value);
            }
            else if(choice.value instanceof Number)
            {
                // if the field type is Object, gson turns all numbers into doubles. Assuming Integer
                choices[i] = new Choice<>(choice.prompt, ((Number) choice.value).intValue());
            }
            else
            {
                throw new RuntimeException(
                        "String and Integer are the only supported values for generating Choices from json");
            }
        }
        return choices;
    }

    public AnswerFormat generateAnswerFormat(CTFStepBuilderHelper helper, String type, JsonObject jsonObject) {

        ChoiceStepDescriptor choiceStepDescriptor = helper.getGson().fromJson(jsonObject, ChoiceStepDescriptor.class);

        AnswerFormat.ChoiceAnswerStyle answerStyle = this.allowsMultiple()
                ? AnswerFormat.ChoiceAnswerStyle.MultipleChoice
                : AnswerFormat.ChoiceAnswerStyle.SingleChoice;

        ChoiceFilter choiceFilter = this.generateFilter(helper, type, jsonObject);
        List<ChoiceStepItemDescriptor> filteredItems = new ArrayList<>();

        for (ChoiceStepItemDescriptor item : choiceStepDescriptor.items) {
            if (choiceFilter.filter(item)) {
                filteredItems.add(item);
            }
        }

        ChoiceAnswerFormat answerFormat = new ChoiceAnswerFormat(answerStyle, this.generateChoices(filteredItems, choiceStepDescriptor.shuffleItems));

        return answerFormat;

    }

}
