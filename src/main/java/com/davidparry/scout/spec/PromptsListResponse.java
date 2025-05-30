package com.davidparry.scout.spec;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

public record PromptsListResponse(@SerializedName("prompts") Set<Prompt> prompts,
                                  @SerializedName("nextCursor") String nextCursor) {

}
