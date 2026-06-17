package com.bloomreach.notification.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Schema(description = "Notification target audience")
public record AudienceRequest(
		@Schema(description = "User ids that should receive the notification", example = "[\"user-1\", \"user-2\"]")
		@NotNull
		List<String> userIds,

		@Schema(description = "Groups that should receive the notification", example = "[\"admins\", \"support\"]")
		@NotNull
		List<String> groups,

		@Schema(description = "Additional audience labels", example = "{\"region\":\"us\",\"tier\":\"gold\"}")
		@NotNull
		Map<String, String> labels) {
}
