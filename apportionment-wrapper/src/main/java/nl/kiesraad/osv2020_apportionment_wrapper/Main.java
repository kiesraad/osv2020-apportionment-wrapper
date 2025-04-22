package nl.kiesraad.osv2020_apportionment_wrapper;

import de.ivu.wahl.result.*;
import de.ivu.wahl.result.builder.CandidatesBuilder;
import de.ivu.wahl.result.builder.ElectionBuilder;
import de.ivu.wahl.result.determination.ElectionResultDeterminator;
import de.ivu.wahl.result.determination.P3List;
import de.ivu.wahl.result.drawlots.Decision;
import de.ivu.wahl.result.drawlots.DrawingLotsCallbackImpl;
import de.ivu.wahl.result.result.ElectionResult;
import de.ivu.wahl.wus.electioncategory.ElectionSubcategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.ivu.wahl.result.builder.ElectionAndCandidatesAndVotesImpl;

import java.util.*;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting ElectionResultDeterminator");
        long[] apportionment = apportionment(
                // number of seats
                18,
                // number of candidates for each political group
                new long[]{19, 5, 4},
                // number of votes for each candidate, in order of the political groups
                new long[]{4878, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 689, 0, 0, 0, 0, 4046, 0, 0, 0}
        );
        logger.info("Apportionment result: {}", Arrays.toString(apportionment));
    }

    /**
     * Apportionment method to determine the election result based on the given parameters.
     *
     * @param seats number of seats
     * @param pgCandidates array with the number of candidates for each political group
     * @param votes array with the votes for each candidate (length should be equal to the sum of pgCandidates)
     * @return an array with the number of seats assigned to each political group, or an array with -1 in case of a conflict (e.g. drawing lots required)
     */
    public static long[] apportionment(long seats, long[] pgCandidates, long[] votes) {
        // Assert: the sum of pgCandidates should be equal to the votes array length
        if (Arrays.stream(pgCandidates).sum() != votes.length) {
            throw new IllegalArgumentException("Sum of pgCandidates should be equal to the length of votes");
        }

        // Create election
        long thresholdNumerator;
        ElectionSubcategory electionSubcategory;
        if (seats < 19) {
            thresholdNumerator = 50;
            electionSubcategory = ElectionSubcategory.GR1;
        } else {
            thresholdNumerator = 25;
            electionSubcategory = ElectionSubcategory.GR2;
        }
        ElectionBuilder eb = new ElectionBuilder(seats, thresholdNumerator, 100, electionSubcategory);
        ElectoralDistrict ed = eb.createElectoralDistrict("TestElection", 1, 1, 1);
        Election election = eb.getElection();
        
        // Create candidate lists and votes map
        Map<CandidateList, long[]> voteMap = new TreeMap<>();
        CandidatesBuilder cb = new CandidatesBuilder(election);

        int voteIdx = 0;
        P3List p3List = null;
        for (int pgNum = 1; pgNum <= pgCandidates.length; pgNum++) {
            int numberOfCandidates = (int) pgCandidates[pgNum - 1];
            String name = "List" + pgNum;
            cb.startCandidateList(1, pgNum, pgNum);
            p3List = cb.createDummyP3ListInAllElectoralDistricts(numberOfCandidates, name + "_", name, pgNum);
            CandidateList cl = p3List.getCandidateLists().iterator().next();
            long[] pgVotes = new long[numberOfCandidates];
            for (int i = 0; i < numberOfCandidates; i++) {
                pgVotes[i] = votes[voteIdx];
                voteIdx += 1;
            }
            logger.info("Votes for {}: {}", cl.getName(), Arrays.toString(pgVotes));
            voteMap.put(cl, pgVotes);
        }

        ElectionAndCandidates eac = cb.getElectionAndCandidates();

        ElectionAndCandidatesAndVotes ecv = new ElectionAndCandidatesAndVotesImpl(eac, voteMap);
        ElectionResult er;
        DrawingLotsCallbackImpl drawingLotsCallback = new DrawingLotsCallbackImpl();
        er = ElectionResultDeterminator.determineElectionResult(drawingLotsCallback, ecv);

        Decision conflict = er.getConflict();
        if (conflict != null) {
            logger.info("Conflict: {}", conflict);
            return new long[]{-1};
        } else {
            logger.info("No conflict");
            return er.getTotalSeatsAssigned()
                    .entrySet()
                    .stream()
                    .sorted(Comparator.comparingInt(x -> x.getKey().getListNumber()))
                    .mapToLong(Map.Entry::getValue).toArray();
        }
    }
}
