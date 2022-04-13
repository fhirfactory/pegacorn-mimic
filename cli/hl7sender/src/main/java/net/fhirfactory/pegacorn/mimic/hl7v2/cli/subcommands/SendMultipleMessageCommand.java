package net.fhirfactory.pegacorn.mimic.hl7v2.cli.subcommands;

import net.fhirfactory.pegacorn.mimic.hl7v2.cli.services.ForwardMLLPMessageService;
import net.fhirfactory.pegacorn.mimic.hl7v2.cli.services.IncrementMLLPMessageService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.charset.StandardCharsets;

@CommandLine.Command(
        name="sendMultipleMessages",
        description="Sends Multiple MLLP Messages based on a file --> with PID, Timestamp and Accession incremented"
)
public class SendMultipleMessageCommand implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(SendSingleMessageCommand.class);
    private Address actualAddress;
    private JChannel locationRPCClient;
    private RpcDispatcher rpcDispatcher;

    @CommandLine.Option(names = {"-f", "--filename"})
    private String fileName;

    @CommandLine.Option(names = {"-h", "--host"})
    private String hostName;

    @CommandLine.Option(names = {"-p", "--port"})
    private Integer port;

    @CommandLine.Option(names = {"-c", "--count"})
    private Integer count;

    @CommandLine.Option(names = {"-g", "--gap"})
    private Long gap;

    @CommandLine.Option(names = {"-i", "--increment"})
    private Boolean changeId;

    @CommandLine.Option(names = {"--givenNameSeed"})
    private String givenSeedName;

    @CommandLine.Option(names = {"--familySeedName"})
    private String familySeedName;

    @CommandLine.Option(names = {"--startingMR"})
    private Integer startingMR;

    @CommandLine.Option(names = {"--accessionNumberSeed"})
    private String accessionNumberSeed;

    String messageString;

    @Override
    public void run() {
        LOG.info(".run(): Entry, fileName --> {}", fileName);
        if( count == null) {
            System.out.println("Must specify a count (-c/--count) value");
        }
        if( gap == null){
            gap = 50L;
        }
        loadMessageFromFile();
        sendMessage();
        System.exit(0);
    }

    public void loadMessageFromFile(){
        LOG.info(".loadMessageFromFile(): Entry");
        try {
            LOG.info(".loadMessageFromFile(): Attempting to Access File");
            File file = new File(fileName);
            LOG.info(".loadMessageFromFile(): Created File Handle...");
            messageString  = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            LOG.info(".loadMessageFromFile(): Message->{}", messageString);
        } catch( Exception ex){
            LOG.info(".loadMessageFromFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
    }

    public void sendMessage(){
        LOG.info(".sendMessage(): Entry");
        ForwardMLLPMessageService mllpForwarder = new ForwardMLLPMessageService(hostName, port);
        LOG.info(".sendMessage(): mllpForwarder created");
        IncrementMLLPMessageService incrementMLLPMessageService = new IncrementMLLPMessageService(givenSeedName, familySeedName,accessionNumberSeed, startingMR);
        LOG.info(".sendMessage(): incrementMLLPMessageService created");
        String actualMessageString = incrementMLLPMessageService.setStartValues(messageString);
        for(Integer sendCount = 0; sendCount < count; sendCount += 1){
            mllpForwarder.sendMessage(actualMessageString);
            actualMessageString = incrementMLLPMessageService.incrementORUMessage(actualMessageString);
            try {
                Thread.sleep(gap);
            } catch(Exception ex){
                // do nothing
            }
        }
    }
}
