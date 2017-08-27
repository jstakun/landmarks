package net.gmsworld.server.test;

import net.gmsworld.server.utils.persistence.LandmarkPersistenceUtils;

import org.junit.Test;

public class PostgresQueryBuilderTest {

	String[] tests = new String[] {
		"United States,Sisksjsks!?x!okjjjj!?juyyggjjjjjnjjj,,,,jjjjjjuuuhbvçgvbbgbjmm!nuhbygfvfcfvfcfcdcffvhhjbjjhygtvfcrfrctgyyygghhuhdessiaisisiskskssishysygssgdgsgdsppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
		"Myanmar (Burma),pathein",
		"Howland Island,test input data!",
		"Mongolia,test input data!",
		"Korea, Republic of (South Korea),Indonesia",
		"united kingdom,Salt 'n pepper",
		"United Kingdom,r r,,,,,,,,x txtnyxnz++,,+ ?4+++66+++++ +++=Va+c nt+5++ :-+?++++ ex ynnyxx+? +.+++ x++hmmx",
		"Ryan Road & Red Run Bridge"
	};
	
	@Test
	public void test() {
		for(int i =0;i<tests.length;i++){
			String queryStr = LandmarkPersistenceUtils.buildQueryString(tests[i]);
			System.out.println(queryStr);
		}
	}

}
