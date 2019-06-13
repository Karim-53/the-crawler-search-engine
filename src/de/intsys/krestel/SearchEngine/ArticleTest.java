package de.intsys.krestel.SearchEngine;
/*
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ArticleTest {

	@Test
	void testStemNTokenize() {
		//tests
		//kim tested it with this example
		final List<String> input = Arrays.asList("Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ\n",
				"Solskjær Mendonça\n", //FIXME: æ char
				"200,000 15-5577 1.5bn 51.1\n",
				"There’s That’s wouldn’t women’s ITV’s \n",
				"O’Neill\n" ,
				"A 2 B&amp\n",
				"i’m here, no here.",
				"F.E.A.R. The_Crawlers:\n",
				"they re-arrange Mercedes-Benz cars.\n",
				"parked in front of a Café, near H&M\n",
				"they don't use $variables after 7 p.m.",
				"with M16A3 in straße\n",
				"men/women day\n",
				"1/2 liter\n",
				"dBm/m\n",
				"George W. Bush we also know how to process normal sentences.\n",
				"nokia 3310\n",
				"(aaa, bbb,ccc)",
				"");
		final List<String> output = Arrays.asList("this is a funky string",
				"solskjoer mendonca", //FIXME: æ char
				"200000 15 5577 155577 1.5bn 51.1",
				"there that wouldnt women itv",
				"oneill" ,
				"a 2 b",
				"im here no here",
				"fear the crawlers",
				"they re arrange rearrange mercedes benz mercedesbenz cars",
				"parked in front of a cafe near h m",
				"they dont use variables after 7 pm",
				"with m16a3 in strasse",
				"men women day",
				"1 2 liter",
				"dbm m",
				"george w bush we also know how to process normal sentences",
				"nokia 3310",
				"aaa bbb ccc",
				"");
		String tokenizedString ;
		String stemmedString;

		//assert(false) : "This should fail";

		for (int i = 0; i < input.size(); i++) {
			tokenizedString = Article.TokenizeTitle(input.get(i));
			
			System.out.println("\nOriginal Version : " + input.get(i));
			//System.out.println("Tokenized Version : " + tokenizedString + "\n");
			stemmedString=Article.PorterStem(tokenizedString);
			//System.out.println(stemmedString);
			System.out.println("Tokenized Version + Stemmed Version : " + stemmedString + "\n");
			assert tokenizedString.compareTo(output.get(i))==0 : tokenizedString + " != " + output.get(i);

		}
		fail("Not yet implemented");
	}

	@Test
	void testTokenizeBody() {
		fail("Not yet implemented");
	}

	@Test
	void testTokenizeTitle() {
		fail("Not yet implemented");
	}

}
*/