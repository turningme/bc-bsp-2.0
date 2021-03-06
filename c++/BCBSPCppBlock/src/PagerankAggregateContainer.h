/*
 * PagerankAggregateContainer.h
 *
 *  Created on: Mar 18, 2014
 *      Author: song jianze
 */


#include <string>
#include <vector>
#include <iterator>
#include "PRTop10Aggregator.h"
using namespace std;

namespace BSPPipes{
class PagerankAggregateContainer : public BSPPipes::AggregatorContainer{

public:
		vector<Aggregator*> Aggregators;
		vector<AggregateValue*> AggregateValues;
		vector<AggregateValue*> CurrentAggregateValues;
		vector<AggregateValue*> AggregateResults;
		PRAggregateValueTop10Node pr_a;
		PRAggregateValueTop10Node pr_ac;
		PRAggregateValueTop10Node pr_ar;
		PRTop10Aggregator prt;
		string test;

	PagerankAggregateContainer(){
		/*ofstream sinfamily;
		sinfamily.open("/home/bcbsp/bc-bsp-0.1/logs/test/AggregateContainerfortest.txt", ios::trunc);
		sinfamily << "----------------pagerank AggregateContainer------ \n-" << endl;
		sinfamily.flush();
		sinfamily.close();*/
		/*PRAggregateValueTop10Node pr_a();
		PRAggregateValueTop10Node pr_ac();
		PRAggregateValueTop10Node pr_ar();*/
	}

     string getTest(){
    	 return  "songjianzhe";
     }
	void loadAggregators() {
		/*ofstream sinfamily;
		sinfamily.open("/home/bcbsp/bc-bsp-0.1/logs/test/AggregateContainerfortest.txt", ios::app);
		sinfamily << "----------------loadAggregators------ \n-" << endl;
		sinfamily.flush();
		sinfamily.close();*/

		//Aggregator* p=&prt;
		this->Aggregators.push_back(&prt);
	}
	void loadAggregateValues(){
		/*ofstream sinfamily;
		sinfamily.open("/home/bcbsp/bc-bsp-0.1/logs/test/AggregateContainerfortest.txt", ios::app);
		sinfamily << "----------------loadAggregateValues------ \n-" << endl;

		//ppp
		sinfamily<<"test for pr_ac: "<<pr_ac.getValue()<<endl;*/
		AggregateValue* p1=&pr_a;
		//AggregateValue* p2=&pr_ac;
		AggregateValue* p3=&pr_ar;
		AggregateValue* p4=new PRAggregateValueTop10Node();
		this->AggregateValues.push_back(p1);
		this->CurrentAggregateValues.push_back(&pr_ac);
		//this->CurrentAggregateValues.push_back(p4);
		this->AggregateResults.push_back(&pr_ar);
		//sinfamily<<"currentAggre size: "<<CurrentAggregateValues.size()<<endl;

		//sinfamily<<"test for CaggV: "<<	CurrentAggregateValues.back()->getValue()<<endl;

		//sinfamily.flush();
		//sinfamily.close();
	}
	vector<Aggregator*> getAggregators(){
		return this->Aggregators;
	}
	vector<AggregateValue*> getAggregateValues(){
		return this->AggregateValues;
	}
	vector<AggregateValue*> getCurrentAggregateValues(){
		return this->CurrentAggregateValues;
	}

	vector<AggregateValue*> getAggregateResults(){
		return this->AggregateResults;
	}

	~PagerankAggregateContainer(){
	};
};
}
