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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    record ApportionmentRequest(
        long seats,
        @SerializedName("pg_candidates") long[] pgCandidates,
        long[] votes
    ) {}

    record SeatsResponse(long[] seats, List<String> log) {}
    record ConflictResponse(boolean conflict, List<String> log) {}
    record ErrorResponse(String error, List<String> log) {}

    static final class DrawingLotsRequiredException extends RuntimeException {
        final Decision decision;
        DrawingLotsRequiredException(Decision d) {
            super("Drawing lots required: " + d);
            this.decision = d;
        }
    }

    public static void main(String[] args) throws Exception {
        Gson gson = new Gson();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = new PrintStream(System.out, true);

        ch.qos.logback.classic.Logger rootLogger =
            (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        String line;
        while ((line = in.readLine()) != null) {
            ListAppender<ILoggingEvent> capture = new ListAppender<>();
            capture.start();
            rootLogger.addAppender(capture);

            try {
                ApportionmentRequest req = gson.fromJson(line, ApportionmentRequest.class);
                long[] result = apportionment(req.seats(), req.pgCandidates(), req.votes());

                List<String> log = capture.list.stream()
                    .map(e -> e.getLevel() + " " + e.getFormattedMessage())
                    .collect(Collectors.toList());
                out.println(gson.toJson(new SeatsResponse(result, log)));
            } catch (DrawingLotsRequiredException e) {
                List<String> log = capture.list.stream()
                    .map(ev -> ev.getLevel() + " " + ev.getFormattedMessage())
                    .collect(Collectors.toList());
                out.println(gson.toJson(new ConflictResponse(true, log)));
            } catch (Exception e) {
                List<String> log = capture.list.stream()
                    .map(ev -> ev.getLevel() + " " + ev.getFormattedMessage())
                    .collect(Collectors.toList());
                out.println(gson.toJson(new ErrorResponse(e.getMessage(), log)));
            } finally {
                capture.stop();
                rootLogger.detachAppender(capture);
            }
        }
    }

    /**
     * Apportionment method to determine the election result based on the given parameters.
     *
     * @param seats number of seats
     * @param pgCandidates array with the number of candidates for each political group
     * @param votes array with the votes for each candidate (length should be equal to the sum of pgCandidates)
     * @return an array with the number of seats assigned to each political group
     * @throws DrawingLotsRequiredException when the result requires drawing lots to resolve a tie
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
        eb.createElectoralDistrict("TestElection", 1, 1, 1);
        Election election = eb.getElection();

        // Create candidate lists and votes map
        Map<CandidateList, long[]> voteMap = new TreeMap<>();
        CandidatesBuilder cb = new CandidatesBuilder(election);

        int voteIdx = 0;
        for (int pgNum = 1; pgNum <= pgCandidates.length; pgNum++) {
            int numberOfCandidates = Math.toIntExact(pgCandidates[pgNum - 1]);
            String name = "List" + pgNum;
            cb.startCandidateList(1, pgNum, pgNum);
            P3List p3List = cb.createDummyP3ListInAllElectoralDistricts(numberOfCandidates, name + "_", name, pgNum);
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
        DrawingLotsCallbackImpl drawingLotsCallback = new DrawingLotsCallbackImpl();
        ElectionResult er = ElectionResultDeterminator.determineElectionResult(drawingLotsCallback, ecv);

        Decision conflict = er.getConflict();
        if (conflict != null) {
            logger.info("Conflict: {}", conflict);
            throw new DrawingLotsRequiredException(conflict);
        }
        logger.info("No conflict");
        return er.getTotalSeatsAssigned()
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(x -> x.getKey().getListNumber()))
                .mapToLong(Map.Entry::getValue).toArray();
    }
}
