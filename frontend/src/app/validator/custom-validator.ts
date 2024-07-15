import { AbstractControl, FormArray, ValidationErrors, ValidatorFn } from "@angular/forms";

export class CustomValidator {
    static notFuture = (ctrl : AbstractControl) => {
        let today : Date = new Date();
        today.setHours(0, 0, 0, 0);
        
        if(new Date(ctrl.value) <= today){
            return null
        }else{
            return {notPast : true} as ValidationErrors
        }
    }

    static confirmPassword(passwordKey: string, confirmPasswordKey: string): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const password = control.get(passwordKey);
            const confirmPassword = control.get(confirmPasswordKey);
            
            if (!password || !confirmPassword) {
                return null;
            }

            if (password.value !== confirmPassword.value) {
                confirmPassword.setErrors({ confirmPasswordMismatch: true });
                return { confirmPasswordMismatch: true };
            } else {
                confirmPassword.setErrors(null);
                return null;
            }
        };
    }

    static minPlayer(minLength: number): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
          if (control instanceof FormArray) {
            return control.length >= minLength ? null : { minPlayer: { valid: false } };
          }
          return null;
        };
    }
}
