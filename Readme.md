Author : Tanmoy Thakur
Description of the project: 

URL on JIRA:  https://jira.ap.manulife.com/browse/DEVO-108
New URL on JIRA: https://jira.ap.manulife.com/browse/DAD-265

Areas of bottleneck: getAllColumGuid -	sequentially fetches the guid for all the columns in the data dictionary.
					Scope for parallel processing

		     setColumnsTag - 	sequentially sets the column tags based on each column guid. Scope for parallel processing

