package de.opendiabetes.vault.exporter.csv;

import de.opendiabetes.vault.data.container.RefinedVaultEntry;
import de.opendiabetes.vault.data.container.RefinedVaultEntryType;
import de.opendiabetes.vault.data.container.VaultEntry;
import de.opendiabetes.vault.data.container.VaultEntryType;
import de.opendiabetes.vault.exporter.ExportEntry;
import de.opendiabetes.vault.util.EasyFormatter;
import de.opendiabetes.vault.util.TimestampUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bucket representation in the data model.
 *
 * @author Raphael Papazikas
 */
public class Bucket implements ExportEntry {

    private static final Logger LOG = Logger.getLogger(Bucket.class.getName());

    public final List<VaultEntry> elements;
    public final Date start;
    public final Date end;
    public final List<VaultEntryType> basicHeader = new ArrayList<>();
    public final List<RefinedVaultEntryType> refinedHeader = new ArrayList<>();

    public static final String NUMBER_COLUMN_NAME = "#";

    /**
     * Constructor for a Bucket instance.
     *
     * @param data list of <VaultEntry>vault entries</VaultEntry> for the bucket
     * @param start the start <Date>date</Date>
     * @param end the end <Date>date</Date>
     */
    public Bucket(List<VaultEntry> data, Date start, Date end) {
        this.elements = data;
        this.start = start;
        this.end = end;
    }

    public Bucket(Date start, Date end) {
        this.elements = new ArrayList<>();
        this.start = start;
        this.end = end;
    }

    /**
     * Transforms data to a bucket entry accourding to saved header fields.
     *
     * @return a row representing the bucket entry.
     */
    public final List<String> buildCsvRow() {
        List<String> row = new ArrayList<>();

        basicHeader.sort(Comparator.<VaultEntryType>naturalOrder());
        refinedHeader.sort(Comparator.<RefinedVaultEntryType>naturalOrder());

        // merge items
        List<VaultEntry> exportEntries = new ArrayList<>();
        for (int i = 0; i < (elements.size() - 1); i++) {
            VaultEntry base = elements.get(i);
            for (int j = i + 1; j < elements.size(); j++) {
                VaultEntry result = merge(base, elements.get(j));
                if (result != null) {
                    base = result;
                }
            }
            exportEntries.add(base);
        }

        // write requested items in requested order
        for (VaultEntryType item : basicHeader) {
            boolean setValue = false;
            for (VaultEntry mergedItem : exportEntries) {
                if (mergedItem.getType() == item) {
                    String tmpString = toBucketString(mergedItem);
                    if (tmpString != null) {
                        row.add(tmpString);
                        setValue = true;
                    }
                    break;
                }
            }
            if (!setValue) {
                // add empty field if no value found
                row.add("");
            }
        }
        for (RefinedVaultEntryType item : refinedHeader) {
            boolean setValue = false;
            for (VaultEntry mergedItem : exportEntries) {
                if (mergedItem.getType() == VaultEntryType.REFINED_VAULT_ENTRY) {
                    RefinedVaultEntry refinedMergedItem = (RefinedVaultEntry) mergedItem;
                    if (refinedMergedItem.getRefinedType() == item
                            && refinedMergedItem.getRefinedType().label
                                    .equalsIgnoreCase(item.label)) {
                        String tmpString = toBucketString(refinedMergedItem);
                        if (tmpString != null) {
                            row.add(tmpString);
                            setValue = true;
                        }
                        break;
                    }
                }
            }
            if (!setValue) {
                if (item.ordinal() == RefinedVaultEntryType.ONE_HOT.ordinal()) {
                    // if one hot encoded, add 0
                    row.add("0");
                } else {
                    // add empty field if no value found
                    row.add("");
                }
            }
        }

        return row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char[] toByteEntryLine() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os = writeStringArray(os, buildCsvRow());

        return os.toString("UTF-8").toCharArray();
    }

    /**
     * Writes a list of strings to the given output stream.
     *
     * @param outputStream stream to write to
     * @param array string list that should be written
     * @return the fiven output stream
     * @throws IOException
     */
    private ByteArrayOutputStream writeStringArray(final ByteArrayOutputStream outputStream,
            final List<String> array) throws IOException {
        ByteArrayOutputStream os = outputStream;
        byte[] delimiterConverted = BucketCsvFileExporter.DELIMITER.getBytes(Charset.forName("UTF-8"));
        byte[] delimiter = new byte[]{};

        for (String line : array) {
            try {
                os.write(delimiter);
                os.write(line.getBytes(Charset.forName("UTF-8")));
                delimiter = delimiterConverted;
            } catch (IOException ex) {
                Logger.getLogger(CsvExportEntry.class.getName()).log(Level.SEVERE,
                        "Error converting String in UTF8", ex);
                throw ex;
            }
        }

        return os;
    }

    /**
     * Creates a csv value from a merged valut entry.
     *
     * @param entry containing data
     * @return CSV field as String
     */
    private static String toBucketString(VaultEntry entry) {
        switch (entry.getType()) {
            // single value is enough
            case BASAL_PROFILE:
            case BASAL_TEMP:
            case BOLUS_SQUARE:
            case BOLUS_NORMAL:
            case MEAL_BOLUS_CALCULATOR:
            case MEAL_MANUAL:
            case PUMP_PRIME:
            case EXERCISE_MANUAL:
            case EXERCISE_OTHER:
            case EXERCISE_LOW:
            case EXERCISE_MID:
            case EXERCISE_HIGH:
            case GLUCOSE_CGM:
            case GLUCOSE_CGM_RAW:
            case GLUCOSE_CGM_ALERT:
            case GLUCOSE_CGM_CALIBRATION:
            case GLUCOSE_BG:
            case GLUCOSE_BG_MANUAL:
            case GLUCOSE_BOLUS_CALCULATION:
            case PUMP_SUSPEND:
            case PUMP_AUTONOMOUS_SUSPEND:
            case PUMP_CGM_PREDICTION:
            case SLEEP_LIGHT:
            case SLEEP_REM:
            case SLEEP_DEEP:
            case HEART_RATE:
            case HEART_RATE_VARIABILITY:
            case STRESS:
            case WEIGHT:
            case KETONES_BLOOD:
            case KETONES_URINE:
            case LOC_TRANSITION:
            case LOC_WORK:
            case LOC_FOOD:
            case LOC_SPORTS:
            case LOC_OTHER:
            case BLOOD_PRESSURE:
                return EasyFormatter.formatDouble(entry.getValue());

            // not supported (--> Events should be encoded as one-hot)
            case CGM_SENSOR_START:
            case CGM_SENSOR_FINISHED:
            case CGM_CONNECTION_ERROR:
            case CGM_CALIBRATION_ERROR:
            case PUMP_REWIND:
            case PUMP_FILL:
            case PUMP_NO_DELIVERY:
            case PUMP_UNSUSPEND:
            case PUMP_UNTRACKED_ERROR:
            case PUMP_RESERVOIR_EMPTY:
            case CGM_TIME_SYNC:
            case PUMP_TIME_SYNC:
            case TAG:
                LOG.warning("Tried to convert unsupported value. Skip.");
                return null;

            // pandora's box
            case REFINED_VAULT_ENTRY:
                // get refined entry
                RefinedVaultEntry refinedEntry = ((RefinedVaultEntry) entry);

                switch (refinedEntry.getRefinedType()) {
                    // value with array
                    case CGM_PREDICTION:
                    case WINDOW:
                        StringBuilder sb = new StringBuilder();
                        sb.append("\"");
                        List<Double> valueList = (List<Double>) refinedEntry.getValueExtension();
                        for (Double item : valueList) {
                            sb.append(EasyFormatter.formatDouble(item)).append(BucketCsvFileExporter.ARRAY_DELIMITER);
                        }
                        sb.delete(sb.length() - BucketCsvFileExporter.ARRAY_DELIMITER.length(),
                                sb.length());
                        sb.append("\"");
                        return sb.toString();

                    // simple value
                    case COB:
                    case IOB_ALL:
                    case IOB_BASAL:
                    case IOB_BOLUS:
                    case NORMALIZED:
                        return EasyFormatter.formatDouble(entry.getValue());

                    // bool
                    case ONE_HOT:
                        return refinedEntry.getValue() <= 0.1 ? "0" : "1";
                    // TODO implement
                    default:
                        LOG.severe("Tried to convert unsupported refined type. Maybe not implemented yet. Skip.");
                        return null;
                }
            default:
                throw new AssertionError("Programming Error! Type not implemented yet.");
        }
    }

    /**
     * Merges information of given objects and returns a merged entry. Merge
     * results are not 100% accurate. Take a look into the functions to
     * understand behaviour.
     *
     * @param base object containing base information.
     * @param merge object containing information to merge.
     * @return merged VaultEntry or null if unmergable.
     */
    private VaultEntry merge(VaultEntry base, VaultEntry merge) {
        if (base.getType() != merge.getType()) {
            // can't merge different types.
            return null;
        }

        VaultEntry returnValue;
        switch (base.getType()) {
            // get merged share for this bucket
            case BASAL_PROFILE:
                // add duration in seconds for easier calculation
                base.setValueExtension(3600.0);
                merge.setValueExtension(3600.0);
            case BASAL_TEMP:
            case BOLUS_SQUARE:
                int minutesOfBaseOverlap = TimestampUtils.getOverlappingMinutes(
                        start, end,
                        base.getTimestamp(),
                        TimestampUtils.addSecondsToTimestamp(base.getTimestamp(),
                                Math.round((Double) base.getValueExtension())));
                int minutesOfMergeOverlap = TimestampUtils.getOverlappingMinutes(
                        start, end,
                        merge.getTimestamp(),
                        TimestampUtils.addSecondsToTimestamp(merge.getTimestamp(),
                                Math.round((Double) merge.getValueExtension())));

                double mergedValue = base.getValue() / minutesOfBaseOverlap;
                mergedValue += merge.getValue() / minutesOfMergeOverlap;

                returnValue = new VaultEntry(base.getType(), base.getTimestamp(),
                        mergedValue);
                return returnValue;

            // sum up values
            case BOLUS_NORMAL:
            case MEAL_BOLUS_CALCULATOR:
            case MEAL_MANUAL:
            case PUMP_PRIME:
                returnValue = new VaultEntry(base.getType(), base.getTimestamp(),
                        base.getValue() + merge.getValue());
                return returnValue;

            // use avg    
            case EXERCISE_MANUAL:
            case EXERCISE_OTHER:
            case EXERCISE_LOW:
            case EXERCISE_MID:
            case EXERCISE_HIGH:
            case GLUCOSE_CGM:
            case GLUCOSE_CGM_RAW:
            case GLUCOSE_CGM_ALERT:
            case GLUCOSE_CGM_CALIBRATION:
            case GLUCOSE_BG:
            case GLUCOSE_BG_MANUAL:
            case GLUCOSE_BOLUS_CALCULATION:
            case PUMP_SUSPEND:
            case PUMP_AUTONOMOUS_SUSPEND:
            case PUMP_CGM_PREDICTION:
            case SLEEP_LIGHT:
            case SLEEP_REM:
            case SLEEP_DEEP:
            case HEART_RATE:
            case HEART_RATE_VARIABILITY:
            case STRESS:
            case WEIGHT:
            case KETONES_BLOOD:
            case KETONES_URINE:
            case LOC_TRANSITION:
            case LOC_WORK:
            case LOC_FOOD:
            case LOC_SPORTS:
            case LOC_OTHER:
            case BLOOD_PRESSURE:
                returnValue = new VaultEntry(base.getType(), base.getTimestamp(),
                        Math.round((base.getValue() + merge.getValue()) / 2));
                return returnValue;

            // nothing to merge
            case CGM_SENSOR_START:
            case CGM_SENSOR_FINISHED:
            case CGM_CONNECTION_ERROR:
            case CGM_CALIBRATION_ERROR:
            case PUMP_REWIND:
            case PUMP_FILL:
            case PUMP_NO_DELIVERY:
            case PUMP_UNSUSPEND:
            case PUMP_UNTRACKED_ERROR:
            case PUMP_RESERVOIR_EMPTY:
                return base;

            // unmergable
            case CGM_TIME_SYNC:
            case PUMP_TIME_SYNC:
            case TAG:
                return null;

            // special treatment for refined entries
            case REFINED_VAULT_ENTRY:
                // check sub type                
                if (((RefinedVaultEntry) base).getRefinedType()
                        != ((RefinedVaultEntry) merge).getRefinedType()
                        || !((RefinedVaultEntry) base).getRefinedType().label
                                .equalsIgnoreCase(((RefinedVaultEntry) merge)
                                        .getRefinedType().label)) {
                    // different types are not mergable
                    return null;
                }

                RefinedVaultEntry refinedBase = ((RefinedVaultEntry) base);
                RefinedVaultEntry refinedMerge = ((RefinedVaultEntry) merge);

                switch (refinedBase.getRefinedType()) {
                    // avg of array
                    case CGM_PREDICTION:
                    case WINDOW:
                        // get smaller window size
                        int windowSize = (int) (refinedBase.getValue() <= refinedMerge.getValue()
                                ? refinedBase.getValue()
                                : refinedMerge.getValue());

                        // merge avg of every single value
                        List<Double> baseValues = (List<Double>) refinedBase.getValueExtension();
                        List<Double> mergeValues = (List<Double>) refinedMerge.getValueExtension();
                        List<Double> avgValues = new ArrayList<>();
                        for (int i = 0; i < windowSize; i++) {
                            double tmpValue = Math.round((baseValues.get(i) + mergeValues.get(i)) / 2 * 100.0) / 100.0;
                            avgValues.add(tmpValue);
                        }

                        RefinedVaultEntry returnEntry = new RefinedVaultEntry(refinedBase.getRefinedType(),
                                refinedBase.getTimestamp(), (double) windowSize);
                        returnEntry.setValueExtension(avgValues);
                        return returnEntry;

                    // use avg
                    case COB:
                    case IOB_ALL:
                    case IOB_BASAL:
                    case IOB_BOLUS:
                    case NORMALIZED:
                        double avgValue = Math.round((refinedBase.getValue() + refinedMerge.getValue()) / 2 * 100.0) / 100.0;
                        return new RefinedVaultEntry(refinedBase.getRefinedType(), refinedBase.getTimestamp(), avgValue);

                    // bool
                    case ONE_HOT:
                        return (refinedBase.getValue() <= 0.1 && refinedMerge.getValue() <= 0.1)
                                ? new RefinedVaultEntry(refinedBase.getRefinedType(), refinedBase.getTimestamp(), 0.0)
                                : new RefinedVaultEntry(refinedBase.getRefinedType(), refinedBase.getTimestamp(), 1.0);

                    default:
                        LOG.warning("Could not merge refined entries. Maybe not implemented yet. Skip.");
                        return null;
                }
            default:
                throw new AssertionError("Programming Error! Type not implemented yet.");
        }
    }

    /**
     * [Helper Function] Creates a <BucketContainer>bucket
     * container</BucketContainer>
     * with a list of buckets regarding to the parameterized interval.
     *
     * @param data the data to build the BucketContainer and the Buckets
     * @param start the start date
     * @param end the end date
     * @param interval to slice in milliseconds
     * @return a <BucketContainer>bucket container</BucketContainer> with the
     * needed parameteres
     */
//    public static final BucketContainer slice(List<VaultEntry> data, Date start, Date end, int interval) {
//        data.sort(new VaultEntryUtils());
//        if (start == null) {
//            start = new Date(data.get(0).getTimestamp().getTime() - 1);
//        }
//        if (end == null) {
//            end = new Date(data.get(data.size() - 1).getTimestamp().getTime() + interval);
//        }
//
//        List<VaultEntry> entries = new ArrayList<>(data);
//        BucketContainer result = new BucketContainer();
//
//        Calendar currentCalender = Calendar.getInstance();
//        currentCalender.setTime(start);
//
//        Calendar endCalendar = Calendar.getInstance();
//        endCalendar.setTime(end);
//
//        long between = ChronoUnit.MILLIS.between(currentCalender.toInstant(), endCalendar.toInstant());
//        int steps = (int) Math.ceil(between / interval);
//
//        endCalendar.setTime(start);
//        endCalendar.add(Calendar.MILLISECOND, (int) interval);
//
//        for (int i = 0; i < steps; i++) {
//            List<VaultEntry> tmp = new ArrayList<>();
//            for (Iterator<VaultEntry> iterator = entries.iterator(); iterator.hasNext();) {
//
//                VaultEntry entry = iterator.next();
//
//                boolean after = entry.getTimestamp().after(currentCalender.getTime());
//                boolean before = entry.getTimestamp().before(endCalendar.getTime());
//                boolean same = entry.getTimestamp().equals(endCalendar.getTime());
//
//                if (same || after && before || (!after && !before)) {
//                    tmp.add(entry);
//                    iterator.remove();
//                }
//            }
//            result.addBucket(new Bucket(tmp, currentCalender.getTime(), endCalendar.getTime()));
//            currentCalender.add(Calendar.MILLISECOND, interval);
//            endCalendar.add(Calendar.MILLISECOND, interval);
//        }
//
//        return result;
//    }
}
