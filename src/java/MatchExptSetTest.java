import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.wcohen.ss.DistanceLearnerFactory;
import com.wcohen.ss.JaroWinkler;
import com.wcohen.ss.api.StringDistanceLearner;
import com.wcohen.ss.expt.Blocker;
import com.wcohen.ss.expt.MatchData;
import com.wcohen.ss.expt.MatchExpt;
import com.wcohen.ss.expt.TokenBlocker;


public class MatchExptSetTest {

	@Test
	public void test() throws IOException {
		Blocker blocker = new TokenBlocker();
		StringDistanceLearner learner = DistanceLearnerFactory.build("JaroWinkler");
		MatchData data = new MatchData();
		data.addInstance("foo", "1", "amy barr");
		data.addInstance("bar","a","amy");
		MatchExpt expt = new MatchExpt(data,learner,blocker);
		expt.dumpResultsAsStrings(System.out);
		data = new MatchData();
		data.addInstance("foo", "2", "burl");
		data.addInstance("bar","b","bob");
		data.addInstance("bar","b2","burl hash");
		expt = new MatchExpt(data,learner,blocker);
		expt.dumpResultsAsStrings(System.out);
	}

}
