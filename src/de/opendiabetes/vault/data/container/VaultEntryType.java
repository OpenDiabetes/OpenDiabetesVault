/**
 * Copyright (C) 2019 Jens Heuschkel
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.opendiabetes.vault.data.container;

/**
 * This enum defines different vault entry types.
 * 
 * @author juehv
 */
public enum VaultEntryType {
    // Bolus
    /**
     * Regular bolus.
     */
    BOLUS_NORMAL,
    /**
     * Squared bolus.
     */
    BOLUS_SQUARE,
    // Basal
    /**
     * Profile basal value.
     */
    BASAL_PROFILE,
    /**
     * Manual basal value.
     */
    BASAL_TEMP,
    // Exercise
    /**
     * Manual exercise.
     */
    EXERCISE_MANUAL,
    /**
     * Other exercise.
     */
    EXERCISE_OTHER,
    /**
     * Low demanding exercise.
     */
    EXERCISE_LOW,
    /**
     * Medium demanding exercise.
     */
    EXERCISE_MID,
    /**
     * Highly demanding exercise.
     */
    EXERCISE_HIGH,
    // Glucose
    /**
     * Continuous glucose monitoring glucose value.
     */
    GLUCOSE_CGM,
    /**
     * Continuous glucose monitoring raw glucose value.
     */
    GLUCOSE_CGM_RAW,
    /**
     * Continuous glucose monitoring glucose alert.
     */
    GLUCOSE_CGM_ALERT,
    /**
     * Continuous glucose monitoring glucose calibration.
     */
    GLUCOSE_CGM_CALIBRATION,
    /**
     * Blood glucose value.
     */
    GLUCOSE_BG,
    /**
     * Manual blood glucose value.
     */
    GLUCOSE_BG_MANUAL,
    /**
     * Glucose bolus calculation.
     */
    GLUCOSE_BOLUS_CALCULATION,
    // Meal
    /**
     * Meal bolus calculator.
     */
    MEAL_BOLUS_CALCULATOR,
    /**
     * Manual meal.
     */
    MEAL_MANUAL,
    // CGM system
    /**
     * Continuous glucose monitoring sensor starting.
     */
    CGM_SENSOR_START,
    /**
     * Continuous glucose monitoring sensor finished.
     */
    CGM_SENSOR_FINISHED,
    /**
     * Continuous glucose monitoring sensor error.
     */
    CGM_CONNECTION_ERROR,
    /**
     * Continuous glucose monitoring calibration error.
     */
    CGM_CALIBRATION_ERROR,
    /**
     * Continuous glucose monitoring time synchronization.
     */
    CGM_TIME_SYNC,
    // Pump Events
    /**
     * Pump rewind event.
     */
    PUMP_REWIND,
    /**
     * Pump prime event.
     */
    PUMP_PRIME,
    /**
     * Pump fill event.
     */
    PUMP_FILL,
    /**
     * No delivery from pump.
     */
    PUMP_NO_DELIVERY,
    /**
     * Pump suspended.
     */
    PUMP_SUSPEND,
    /**
     * Autonomous pump suspension.
     */
    PUMP_AUTONOMOUS_SUSPEND,
    /**
     * Pump unsuspended.
     */
    PUMP_UNSUSPEND,
    /**
     * Untracked pump error.
     */
    PUMP_UNTRACKED_ERROR,
    /**
     * Pump's reservoir empty.
     */
    PUMP_RESERVOIR_EMPTY,
    /**
     * Pump time synchronization.
     */
    PUMP_TIME_SYNC,
    /**
     * Continuous glucose monitoring pump prediction.
     */
    PUMP_CGM_PREDICTION,
    // Sleep
    /**
     * Light sleep.
     */
    SLEEP_LIGHT,
    /**
     * REM sleep.
     */
    SLEEP_REM,
    /**
     * Deep sleep.
     */
    SLEEP_DEEP,
    // Heart
    /**
     * Heart rate value.
     */
    HEART_RATE,
    /**
     * Heart rate variability.
     */
    HEART_RATE_VARIABILITY,
    /**
     * Stress.
     */
    STRESS,
    /**
     * Weight.
     */
    WEIGHT,
    // Ketones
    /**
     * Blood ketones.
     */
    KETONES_BLOOD,
    /**
     * Urine ketones.
     */
    KETONES_URINE,
    // Location (Geocoding)
    /**
     * Location transition.
     */
    LOC_TRANSITION,
    /**
     * Home location.
     */
    LOC_HOME,
    /**
     * Work location.
     */
    LOC_WORK,
    /**
     * Food location.
     */
    LOC_FOOD,
    /**
     * Sports location.
     */
    LOC_SPORTS,
    /**
     * Other location.
     */
    LOC_OTHER,
    //Blood Pressure
    /**
     * Blood pressure.
     */
    BLOOD_PRESSURE,
    //Tags
    /**
     * User defined tags, saved by an app.
     */
    TAG,
    // Refined Vault Entry
    REFINED_VAULT_ENTRY;

    public static VaultEntryType valueOfIgnoreCase(String string) {
        for (VaultEntryType item : VaultEntryType.values()) {
            if (item.toString().equalsIgnoreCase(string)) {
                return item;
            }
        }
        return null;
    }
}

// For faster switch-case statements
//            case BOLUS_NORMAL:
//            case BOLUS_SQUARE:
//            case BASAL_PROFILE:
//            case BASAL_TEMP:
//            case EXERCISE_MANUAL:
//            case EXERCISE_OTHER:
//            case EXERCISE_LOW:
//            case EXERCISE_MID:
//            case EXERCISE_HIGH:
//            case GLUCOSE_CGM:
//            case GLUCOSE_CGM_RAW:
//            case GLUCOSE_CGM_ALERT:
//            case GLUCOSE_CGM_CALIBRATION:
//            case GLUCOSE_BG:
//            case GLUCOSE_BG_MANUAL:
//            case GLUCOSE_BOLUS_CALCULATION:
//            case MEAL_BOLUS_CALCULATOR:
//            case MEAL_MANUAL:
//            case CGM_SENSOR_START:
//            case CGM_SENSOR_FINISHED:
//            case CGM_CONNECTION_ERROR:
//            case CGM_CALIBRATION_ERROR:
//            case CGM_TIME_SYNC:
//            case PUMP_REWIND:
//            case PUMP_PRIME:
//            case PUMP_FILL:
//            case PUMP_NO_DELIVERY:
//            case PUMP_SUSPEND:
//            case PUMP_AUTONOMOUS_SUSPEND:
//            case PUMP_UNSUSPEND:
//            case PUMP_UNTRACKED_ERROR:
//            case PUMP_RESERVOIR_EMPTY:
//            case PUMP_TIME_SYNC:
//            case PUMP_CGM_PREDICTION:
//            case SLEEP_LIGHT:
//            case SLEEP_REM:
//            case SLEEP_DEEP:
//            case HEART_RATE:
//            case HEART_RATE_VARIABILITY:
//            case STRESS:
//            case WEIGHT:
//            case KETONES_BLOOD:
//            case KETONES_URINE:
//            case LOC_TRANSITION:
//            case LOC_WORK:
//            case LOC_FOOD:
//            case LOC_SPORTS:
//            case LOC_OTHER:
//            case BLOOD_PRESSURE:
//            case TAG:
//            case REFINED_VAULT_ENTRY:
