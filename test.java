@startuml SAP_S4_GL_Integration
title SAP S4 GL - Integration Architecture

' ============================================================
'  Layout & global styling
' ============================================================
left to right direction
skinparam shadowing false
skinparam componentStyle rectangle
skinparam roundCorner 4
skinparam ArrowColor #000000
skinparam ArrowFontColor #000000

' Colour coding taken from the source diagram legend
skinparam rectangle {
  BackgroundColor<<new>>        #d5e8d4
  BorderColor<<new>>            #82b366
  BackgroundColor<<existing>>   #ffffff
  BorderColor<<existing>>       #000000
  BackgroundColor<<enhanced>>   #ffe6cc
  BorderColor<<enhanced>>       #d79b00
  BackgroundColor<<deprecated>> #f8cecc
  BorderColor<<deprecated>>     #b85450
  BackgroundColor<<group>>      #ffffff
  BorderColor<<group>>          #666666
}

' ============================================================
'  Upstream Applications (Internal)
' ============================================================
rectangle "Upstream Applications\n(Internal)" <<group>> as UP {
  rectangle "Active Directory" <<existing>> as AD
  rectangle "PSGL"             <<existing>> as PSGL
  rectangle "FPSL"             <<existing>> as FPSL
  rectangle "Long View (Tax)"  <<existing>> as LV_UP
  rectangle "Axiom"            <<existing>> as AXIOM
  rectangle "Success Factor"   <<existing>> as SF
}

' ============================================================
'  SAP S4 GL  (New Component)
' ============================================================
rectangle "SAP S4 GL" <<new>> as S4 {

  rectangle "Business Country Supported" <<new>> as BCS {
    rectangle "Korea" <<new>> as KOREA
  }

  rectangle "Business Capabilities / Functions" <<new>> as BCF {
    rectangle "Core HR"      <<new>> as CORE_HR
    rectangle "Job"          <<new>> as JOB
    rectangle "Time-Absence" <<new>> as TIME_ABS
    rectangle "Payroll"      <<new>> as PAYROLL
  }
}

' ============================================================
'  Downstream Applications (Internal)
' ============================================================
rectangle "Downstream Applications\n(Internal)" <<group>> as DOWN {
  rectangle "Long View (Tax)"  <<existing>> as LV_DOWN
  rectangle "Indonesia S4"     <<existing>> as ID_S4
  rectangle "Indonesia Local"  <<existing>> as ID_LOCAL
}

' ============================================================
'  Interfaces  (solid = automated interface)
'  To mark a New interface:       AD -[#82b366]-> S4
'  Manual / semi-automated:       AD -[#82b366,dashed]-> S4
'  Enhanced / Deprecated: use #d79b00 / #b85450
' ============================================================
AD     --> S4 : Data Entities E1
PSGL   --> S4 : Data Entities E1
FPSL   --> S4 : Data Entities E1
LV_UP  --> S4 : Data Entities E1
AXIOM  --> S4 : Data Entities E1
SF     --> S4 : Data Entities E1

S4 --> LV_DOWN : Data Entities E2
S4 --> ID_S4   : Data Entities E3
S4 --> ID_LOCAL: Data Entities E4

' ============================================================
'  Legend
' ============================================================
legend right
  <b>Component or Service</b>
  |<#d5e8d4>      | New |
  |<#ffffff>      | Existing |
  |<#ffe6cc>      | Enhanced |
  |<#f8cecc>      | Deprecated |
  <b>Interface</b>: solid = Automated, dashed = Manual / Semi-Automated
endlegend

@enduml
