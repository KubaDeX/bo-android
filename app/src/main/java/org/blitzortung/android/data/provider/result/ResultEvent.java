package org.blitzortung.android.data.provider.result;

import org.blitzortung.android.data.Parameters;
import org.blitzortung.android.data.beans.RasterParameters;
import org.blitzortung.android.data.beans.Station;
import org.blitzortung.android.data.beans.StrikeAbstract;

import java.util.List;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode
public class ResultEvent implements DataEvent {

    private final List<StrikeAbstract> strikes;

    private final List<Station> stations;

    private final RasterParameters rasterParameters;

    private final int[] histogram;

    private final boolean fail;

    private final boolean incrementalData;

    private final long referenceTime;

    private final Parameters parameters;

    public boolean containsStrikes() {
        return !strikes.isEmpty();
    }

    public boolean containsParticipants() {
        return stations != null;
    }

    public boolean hasFailed() {
        return fail;
    }

    public boolean hasRasterParameters() {
        return rasterParameters != null;
    }

    public boolean containsRealtimeData() {
        return parameters != null && parameters.getIntervalOffset() == 0;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fail) {
            sb.append("FailedResult()");
        } else {
            sb.append("Result(");
            final List<StrikeAbstract> currentStrikes = getStrikes();
            sb.append(currentStrikes != null ? currentStrikes.size() : 0).append(" strikes, ");
            sb.append(getParameters());
            if (hasRasterParameters()) {
                sb.append(", ").append(getRasterParameters());
            }
            sb.append(")");
        }

        return sb.toString();
    }
}