package com.vogella.spring.datacrawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vogella.spring.data.entities.Bug;
import com.vogella.spring.data.repositories.BugRepository;
import com.vogella.spring.datacrawler.communication.BugzillaController;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Class to export database files into an arff file. This file can be used to
 * import into Weka, a data mining tool. This allows to easy "play" with the
 * data (try different algorithms, feature sets, etc) without doing much
 * programming work.
 * 
 * @author david
 *
 */
@Component
public class ArffFileExporter {

	private static final Logger logger = Logger.getLogger(BugzillaController.class.getName());

	@Autowired
	BugRepository bugRepository;

	public ArffFileExporter() {
	}

	public void exportBugData() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.log(Level.INFO, "Start to create file");
				Instances instances = new Instances("MyRelation", getAttributes(), 0);
				bugRepository.findAll().forEach(bug -> instances.add(getInstance(bug, instances)));
				writeToFile(instances);
				logger.log(Level.INFO, "File created");
			}
		});
		t.run();
	}

	private void writeToFile(Instances instances) {
		File file = new File("exportedBugData.arff");
		try {
			FileWriter fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(instances);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// // string
		// attributes.add(new Attribute("title", true));

		// nominal
		attributes.add(new Attribute("component", ((ArrayList<String>) bugRepository.findAllDistinctComponents())));

		attributes.add(new Attribute("reporter", ((ArrayList<String>) bugRepository.findAllDistinctReporters())));

		attributes.add(new Attribute("assignedTo", ((ArrayList<String>) bugRepository.findAllDistinctAssignedTo())));

		// attributes.add(new Attribute("creationTimestamp", true));
		//
		// attributes.add(new Attribute("lastChangeTimestamp", true));

		attributes.add(new Attribute("version", ((ArrayList<String>) bugRepository.findAllDistinctVersions())));

		attributes.add(new Attribute("reportedPlatform",
				((ArrayList<String>) bugRepository.findAllDistinctReportedPlatforms())));

		attributes.add(new Attribute("operationSystem",
				((ArrayList<String>) bugRepository.findAllDistinctOperationSystems())));

		// attributes.add(new Attribute("status", true));
		//
		// attributes.add(new Attribute("resolution", true));

		attributes.add(new Attribute("milestone", ((ArrayList<String>) bugRepository.findAllDistinctMilestones())));

		// numeric
		attributes.add(new Attribute("numberCc"));

		attributes.add(new Attribute("numberAdditionalLinks"));

		attributes.add(new Attribute("numberAttachments"));

		attributes.add(new Attribute("numberComments"));

		attributes.add(new Attribute("numberBlocks"));

		attributes.add(new Attribute("numberDependsOn"));

		ArrayList<String> priorities = new ArrayList<>();
		priorities.add("high");
		priorities.add("medium");
		priorities.add("low");
		attributes.add(new Attribute("priority", priorities));

		attributes.add(new Attribute("severity", ((ArrayList<String>) bugRepository.findAllDistinctSeverities())));

		attributes.add(new Attribute("votes"));

		return attributes;
	}

	private Instance getInstance(Bug bug, Instances instances) {
		Instance instance = new DenseInstance(instances.numAttributes());
		instance.setDataset(instances);

		// instance.setValue(instances.attribute("title"), bug.getTitle());
		instance.setValue(instances.attribute("component"), bug.getComponent());
		instance.setValue(instances.attribute("reporter"), bug.getReporter());
		instance.setValue(instances.attribute("assignedTo"), bug.getAssignedTo());
		// instance.setValue(instances.attribute("creationTimestamp"),
		// bug.getCreationTimestamp().toString());
		// instance.setValue(instances.attribute("lastChangeTimestamp"),
		// bug.getLastChangeTimestamp().toString());
		instance.setValue(instances.attribute("priority"), getMappedPriority(bug.getPriority()));
		instance.setValue(instances.attribute("severity"), bug.getSeverity());
		instance.setValue(instances.attribute("version"), bug.getVersion());
		instance.setValue(instances.attribute("reportedPlatform"), bug.getReportedPlatform());
		instance.setValue(instances.attribute("operationSystem"), bug.getOperationSystem());
		// instance.setValue(instances.attribute("status"), bug.getStatus());
		// instance.setValue(instances.attribute("resolution"), bug.getResolution());
		instance.setValue(instances.attribute("milestone"), bug.getMilestone());
		instance.setValue(instances.attribute("numberCc"), bug.getCcList().size());
		instance.setValue(instances.attribute("numberAdditionalLinks"), bug.getAdditionalLinks().size());
		instance.setValue(instances.attribute("numberAttachments"), bug.getAttachments().size());
		instance.setValue(instances.attribute("numberComments"), bug.getComments().size());
		instance.setValue(instances.attribute("numberBlocks"), bug.getBlocks().size());
		instance.setValue(instances.attribute("numberDependsOn"), bug.getDependsOn().size());
		instance.setValue(instances.attribute("votes"), bug.getVotes());

		return instance;
	}

	private String getMappedPriority(String priority) {
		if (priority.equals("P1") || priority.equals("P2")) {
			return "high";
		} else if (priority.equals("P4") || priority.equals("P5")) {
			return "low";
		} else {
			return "medium";
		}
	}
}