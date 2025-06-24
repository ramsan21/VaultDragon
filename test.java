@startuml
box "Internal" #LightGray
participant S2B
participant UAAS
end box

actor User
participant "External Application" as ExtApp

== Authorization Flow ==

User -> ExtApp : [001] Authorizaation request
ExtApp -> User : [002] Login to External Application\nwith valid session

ExtApp -> S2B : [003] /oauth/v2/authorize
S2B -> UAAS : 
UAAS --> S2B : [004] authorization code
S2B -> ExtApp : [005] Authorization code

ExtApp -> S2B : [006] /oauth/v2/token
S2B -> UAAS : [007] /oauth/v2/token
UAAS --> S2B : [008] access token response
S2B -> ExtApp : [009] access token

@enduml