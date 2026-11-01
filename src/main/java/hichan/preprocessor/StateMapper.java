package hichan.preprocessor;

import hichan.common.Config;

/**
 * Converts NHPN STFIPS codes to two-letter state abbreviations (FN6.3, Detailed Design 3.3).
 */
public class StateMapper {

    /**
     * Returns the state code for a FIPS number, for example 53 to "WA".
     *
     * @throws IllegalArgumentException if the code is not WA or ID
     */
    public String toStateCode(int stateFips) {
        String code = Config.STATE_CODES.get(stateFips);
        if (code == null) {
            throw new IllegalArgumentException("Unknown STFIPS code: " + stateFips);
        }
        return code;
    }
}