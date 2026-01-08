package com.fieldops.fieldops_api.asset.model;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.fieldops.fieldops_api.asset.service.AssetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.servlet.HandlerMapping;

/** Validate that the serialNumber value isn't taken yet. */
@Target({FIELD, METHOD, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AssetSerialNumberUnique.AssetSerialNumberUniqueValidator.class)
public @interface AssetSerialNumberUnique {

  String message() default "{Exists.asset.serialNumber}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class AssetSerialNumberUniqueValidator
      implements ConstraintValidator<AssetSerialNumberUnique, String> {

    private final AssetService assetService;
    private final HttpServletRequest request;

    public AssetSerialNumberUniqueValidator(
        final AssetService assetService, final HttpServletRequest request) {
      this.assetService = assetService;
      this.request = request;
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
      if (value == null) {
        // no value present
        return true;
      }
      @SuppressWarnings("unchecked")
      final Map<String, String> pathVariables =
          ((Map<String, String>)
              request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
      final String currentId = pathVariables.get("id");
      if (currentId != null
          && value.equalsIgnoreCase(
              assetService.get(UUID.fromString(currentId)).getSerialNumber())) {
        // value hasn't changed
        return true;
      }
      return !assetService.serialNumberExists(value);
    }
  }
}
