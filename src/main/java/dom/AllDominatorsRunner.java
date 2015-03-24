package dom;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.io.BinaryValueReader;

public class AllDominatorsRunner {

	public static final boolean LOG_BINARY_RESULTS = false;
	public static final boolean LOG_TEXTUAL_RESULTS = false;
	
	public static final String DATA_SET_SINGLE_FILE_NAME = "data/single.bin";
	
	public static final String DATA_SET_SAMPLED_FILE_NAME = "data/wordpress-cfgs-as-graphs-sampled.bin";
	public static final String DATA_SET_FULL_FILE_NAME = "data/wordpress-cfgs-as-graphs.bin";
	
	public static final String CURRENT_DATA_SET_FILE_NAME = DATA_SET_FULL_FILE_NAME;

	public static final IMap CURRENT_DATA_SET;

	static {
		IValueFactory vf = org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance();
		
		try {
			CURRENT_DATA_SET = (IMap) new BinaryValueReader().read(vf, new FileInputStream(
							CURRENT_DATA_SET_FILE_NAME));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		System.err.println("Global data initialized.");
		System.err.println();
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		IMap resultsBareMetalOne = DominatorsWithoutPDB.testOne();
		IMap resultsClojureOne = DominatorsClojure.testOne();
		IMap resultsScalaOne = DominatorsScalaV1.testOne();
//		IMap resultsPdbPersistentOne = DominatorsPDB.testOne(org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance());
// 		IMap resultsPdbFastOne = DominatorsPDB.testOne(org.eclipse.imp.pdb.facts.impl.fast.ValueFactory.getInstance());		
//		
//		if (!resultsPdbPersistentOne.equals(resultsBareMetalOne)) {
//			throw new Error("Dominator calculations do differ [PDB Persistent vs CHART]!");
//		} else {
//			System.err.println("Are equal!\n\n");
//		}

		if (!resultsBareMetalOne.equals(resultsScalaOne)) {
			throw new Error("Dominator calculations do differ [CHART vs Scala]!");
		} else {
			System.err.println("[CHART and Scala] are equal!\n\n");
		}
		
		if (!resultsBareMetalOne.equals(resultsClojureOne)) {
			throw new Error("Dominator calculations do differ [CHART vs Clojure]!");
		} else {
			System.err.println("[CHART and Clojure] are equal!\n\n");
		}		
		
		ISet resultsBareMetal = DominatorsWithoutPDB.testAll();
		ISet resultsClojure = DominatorsClojure.testAll();
		ISet resultsScala = DominatorsScalaV1.testAll();
//		ISet resultsPdbPersistent = DominatorsPDB.testAll(org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory.getInstance());
// 		ISet resultsPdbFast = DominatorsPDB.testAll(org.eclipse.imp.pdb.facts.impl.fast.ValueFactory.getInstance());		
//		
//		if (!resultsPdbPersistent.equals(resultsBareMetal)) {
//			throw new Error("Dominator calculations do differ [PDB Persistent vs CHART]!");
//		} else {
//			System.err.println("Are equal!\n\n");
//		}
		
		if (!resultsBareMetal.equals(resultsScala)) {
			throw new Error("Dominator calculations do differ [CHART vs Scala]!");
		} else {
			System.err.println("[CHART and Scala] are equal!\n\n");
		}
		
		if (!resultsBareMetal.equals(resultsClojure)) {
			throw new Error("Dominator calculations do differ [CHART vs Clojure]!");
		} else {
			System.err.println("[CHART and Clojure] are equal!\n\n");
		}		
	}

}
