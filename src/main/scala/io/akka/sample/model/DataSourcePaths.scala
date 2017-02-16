package io.akka.sample.model

import java.nio.file.Paths

object DataSourcePaths {
  
  /*
        1.4M normal_20170202_2229.csv
        2.0M pre-fail_20170202_2234.csv
        
         15M state_0_loop_0.csv
         12M state_1_loop_1.csv
         
        2.4M verify_0.csv
        1.1M verify_20170202_2243.csv
   */
  
  val Normal_20170202_2229 = Paths.get("data", "normal_20170202_2229.csv")
  val PreFail_20170202_2234 = Paths.get("data", "pre-fail_20170202_2234.csv")
  
  val State_Good = Paths.get("data", "state_0_loop_0.csv")
  val State_Bad_Loop_1 = Paths.get("data", "state_1_loop_1.csv")
  
  val Verify_0 = Paths.get("data", "verify_0.csv")
  val Verify_20170202_2243 = Paths.get("data", "verify_20170202_2243.csv")
}
