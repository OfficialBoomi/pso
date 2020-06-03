/*
------------------------------------------------------------
Script Name: [FWK] Generic If/Else Conditional Mapping
     Author: Alexander Schwer (Boomi EMEA PSO)
    Purpose: Provides generic if/else conditional mapping capability for string comparison
   Input(s): operator (Script Variable - Input). Allowed values: "==" for equal comparison, or "!=" for not equal comparison
             inputValue1 (Script Variable - Input)
             inputValue2 (Script Variable - Input)
             outputIfTrue (Script Variable - Input)
             outputIfFalse (Script Variable - Input)
  Output(s): outputValut (Script Variable - Output)
    Note(s): only usable as map script with correctly defined input/output variables
------------------------------------------------------------
Version Control:
14/02/2019 - Alexander Schwer: initial version
02/06/2020 - Rebecca Koppen: fixed error message to show operator

------------------------------------------------------------
*/

//Equal comparison
if (operator == "=="){
    if (inputValue1 == inputValue2) {
        outputValue = outputIfTrue;
    } 
    else {
        outputValue = outputIfFalse;
    }
}
//Not-Equal comparison
else if (operator == "!="){
    if (inputValue1 != inputValue2) {
        outputValue = outputIfTrue;
    } 
    else {
        outputValue = outputIfFalse;
    }
}
//Unsupported operator error
else {
    throw new Error("Unknown operator: "  + operator + ". Must be either == or !=");
}
