package de.intsys.krestel.SearchEngine;
/*


Map<String, Map<Integer, Integer>> m = new HashMap<>();
		Map<Integer, Integer> x = new HashMap<>();
		x.put(5,3);
		m.put("a",x);
		x.put(0,0);
		System.out.println( m.get("a") ) ;


String a  = "the venezuelan president nicolás maduro has castigated the european union and accused washington of waging an imperialist “world war” against his crisis-stricken nation as he shrugged off a tempest of international condemnation to begin his second term in office. maduro who inherited hugo chávez’s bolivarian revolution after his 2013 death has overseen a calamitous decline in his country’s fortunes and was re-elected in disputed elections last may. a shower of domestic and international censure met the 56-year-old’s swearing in at the supreme court in caracas on thursday. the united states secretary of state mike pompeo condemned maduro’s “illegitimate usurpation of power” and vowed to “use the full weight of us economic and diplomatic power to press for the restoration of venezuelan democracy”. the european union called last year’s vote “neither free nor fair” and said maduro was “starting a new mandate on the basis of non-democratic elections”. latin american governments also denounced the inauguration with paraguay breaking off diplomatic ties and argentina’s president mauricio macri branding maduro “the victimizer who plays victim”. “venezuela is living under a dictatorship ” macri tweeted. maduro defied those attacks during a feisty 80-minute address to fellow chavistas and international leftist allies including the nicaraguan president daniel ortega bolivia’s evo morales and cuba’s miguel díaz-canel. “we are a true profound popular and revolutionary democracy … not a democracy of the elites … of super-millionaires who go into power to enrich their economic group and to rob the people ” he claimed. “and i nicolás maduro moros am a genuinely and profoundly democratic president.” maduro who is facing growing regional pressure as latin american politics swerves back to the right claimed his country was “at the centre of a world war” being waged by us imperialists and their “satellite governments” in latin america. he assailed colombia’s president iván duque and brazil’s new far-right leader jair bolsonaro calling him a “fascist”. maduro also attacked the european union. “stop europe … don’t come again with your old colonialism. don’t come again with your old aggression. don’t come again old europe with your old racism ” he said. “there’s been enough enslavement – the looting that you subjected us to for 500 years … respect venezuela … or sooner rather than later you’ll pay the historical price.” but there was also public acceptance that chavistas – implicated in a succession of eye-watering corruption scandals – had a hand in venezuela’s collapse. “i want a new start for the bolivarian revolution … i want us to correct the many mistakes we have committed ” maduro said calling corrupt chavistas a greater threat than us imperialism. he promised “a great moral revolution” and “a profound correction of the mistakes of the bolivarian revolution” in his second term. “we are the heirs of a great legacy. we cannot fail and we will not fail ” maduro concluded to loud applause. amid heavy security red-clad chavistas gathered outside the court in downtown caracas surrounded by posters reading: “yo soy presidente” – “i’m the president”. “i identify with maduro because he’s a humble man like me. my ideology is what brings me here today ” said jesús alcalá a 44-year-old craftsman. others were obliged to appear. “i was forced to come today ” said one 30-year-old housing ministry employee and who asked not to be named for fear of dismissal. “i’m not happy with this situation ” she confided. “i have three kids and i really struggle to feed them. i’m planning to leave the country this year.” on state television channels the socialist party faithful lined up to defend their embattled leader. “lots of countries are saying that our president is illegitimate – they are wrong ” a woman named as sory ramos told telesur. maría alejandra díaz a member of venezuela’s powerful constituent assembly urged maduro’s opponents to reject what she called foreign plots to unseat her leader. “do not invite the demon into your home. never ask a foreign power to intervene or invade ” she warned. maduro’s term is due to last six years but many doubt he will make it that far such are the economic and political headwinds buffeting his country. last year inflation reportedly hit 1.35 million percent while a mass exodus that has seen almost 10% of the country abandon the country continued. “economically we are in a death spiral ” said phil gunson a caracas-based expert for crisis group. “there is a real sense that the whole country is grinding to a halt – and a cold analysis has to reach the conclusion that it is impossible to go on like this.” but with the opposition “shattered into a thousand pieces” gunson admitted there was no sign of even a midterm political or economic fix. “some people say the government may collapse because it has collapsed the whole country and there is no way it can continue. but if there is nothing to take over from it then even if it’s collapsed it will remain in power.” david smilde a venezuela expert from the washington office on latin america advocacy group said maduro hoped forging “an alternative network” of partnerships with authoritarian countries including china russia and turkey would enable him to weather the storm. “they think … that if they can just keep holding on within a couple of years they can wait this out and countries will get tired of pressuring venezuela eventually things will normalise. i think they’re just trying really to hang it out without any great master plan.”";
		String exactquery = "great legacy";
		System.out.println(   a.indexOf(exactquery)   );


		String s = "A short US award winning documentary has been made about Katie’s recovery – https://youtu.be/lvViuv0vIKU – & she has since been on a mission to help others like us who might now be suffering as she was.";
		System.out.println( Article.tokenizeMinimumChange(s) );

List<Integer> foo = new ArrayList<>();
        foo.add(38981);
        Article.getHeavyArticlesFromID( foo, searchEngineTheCrawlers.idxDico);







//System.out.println( Article.TokenizeTitle("pedro sánchez")  );

//System.out.println(   InvertedIndexer.getPostingList("mistreat", searchEngineTheCrawlers.idxDico)   );
//System.out.println(   InvertedIndexer.getPostingList("illumin", searchEngineTheCrawlers.idxDico)   );



/*for(int i =38981 ; i <39021;i++ ) {
			System.out.println(i);
			List<Integer> foo = new ArrayList<Integer>();
			foo.add(i);
			Article.getLightArticlesFromID(foo, searchEngineTheCrawlers.idxDico.articleId_To_LightArticlePos);//
		}//*/


/*small test
		 * Article article = new Article( 0, "","",Arrays.asList(";".split(Constants.LIST_SEPARATOR)),"aaaaa aaaaa bbbbbb","","",Arrays.asList(";".split(Constants.LIST_SEPARATOR)));

		article.stemNTokenize();
		Set<String> tok = article.getUniqueTokens();
		System.out.println(tok.size());
		System.out.println(article.toString());
		System.out.println(article.getNonUniqueTokens().length);
		*/

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