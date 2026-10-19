[Start]
{
        [Draw]
        {
                type=Button
                TwoDArea=0,0,0,0
                ToolTip="*"
        }       

        shortcut = ShowMouseAreas             , KEY_O
        shortcut = Continue     , KEY_RETURN
        shortcut = Console    , KEY_BACK_QUOTE
        shortcut = Escape, KEY_ESCAPE

        [Scene]
        {
                type=Scene
                Render_Screen_Model=0
                DeleteZBuffer=TRUE
        }

        [Button1]
        {
                type=Button
                TwoDArea=37,61,246,51
                ToolTip="#iTOOL_112#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [Button2]
        {
                type=Button
                TwoDArea=37,152,246,40
                ToolTip="#iTOOL_113#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [Button3]
        {
                type=Button
                TwoDArea=37,244,246,37
                ToolTip="#iTOOL_114#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [Button4]
        {
                type=Button
                TwoDArea= 37,320,246,36
                ToolTip="#iTOOL_115#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [Button5]
        {
                type=Button
                TwoDArea= 37,419,246,38
                ToolTip="#iTOOL_116#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [CreateServerButton]
        {
                type=Button
                TwoDArea=48,59,207,50
                ToolTip="#iTOOL_117#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [JoinButton]
        {
                type=Button
                TwoDArea=91,152,137,40
                ToolTip="#iTOOL_118#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [StartGamespyButton]
        {
                type=Button
                TwoDArea=49,243,212,35
                ToolTip="#iTOOL_119#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [MultiplayerBackButton]
        {
                type=Button
                TwoDArea=117,332,87,32
                ToolTip="#iTOOL_120#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [CampaignButton]
        {
                type=Button
                TwoDArea=65,61,173,44
                ToolTip="#iNTOOL_121#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ScenarioButton]
        {
                type=Button
                TwoDArea=67,148,173,44
                ToolTip="#iTOOL_122#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [RandomMapButton]
        {
                type=Button
                TwoDArea=70,238,173,44
                ToolTip="#iTOOL_123#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [SinglePlayerBackButton]
        {
                type=Button
                TwoDArea=76,328,173,44
                ToolTip="#iTOOL_124#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [JoinIPButton]
        {
                type=Button
                TwoDArea=23,395,295,70
                ToolTip="#iTOOL_125#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [BackFromJoinButton]
        {
                type=Button
                TwoDArea=23,491,295,70
                ToolTip="#iTOOL_126#"
;		onclicksound =Sound/screens/Button-37.wav

        }       

        [ReadyButton]
        {
                type=Button
                TwoDArea=29,517,180,38
                ToolTip="#iTOOL_127#"
        }       

        [BackFromReadyButton]
        {
                type=Button
                TwoDArea=29,554,180,38
                ToolTip="#iTOOL_128#"
        }       

        [LoadButton]
        {
                type=Button
                TwoDArea=33,421,296,47
                ToolTip="#iTOOL_129#"
        }

        [LoadBackButton]
        {
                type=Button
                TwoDArea=24,505,296,47
                ToolTip="#iTOOL_130#"
        }
             
        [LoadList]
        {
                type=ListBox
                TwoDArea=7,16,323,350
        }

        [CampaignButton1]
        {
                type=Button
                TwoDArea=0,53,328,50
                ToolTip="#iTOOL_131#"
                onclicksound =Sound/screens/Button-09.wav
        }

[CampaignButton2]
{
type=Button
TwoDArea=0,140,328,50
ToolTip="#iTOOL_132#"
		onclicksound =Sound/screens/Button-09.wav

}

[CampaignButton3]
{
type=Button
TwoDArea=0,227,318,50
ToolTip="#iTOOL_133#"
		onclicksound =Sound/screens/Button-09.wav

}

[CampaignButton4]
{
type=Button
TwoDArea=0,311,318,50
ToolTip="#iTOOL_134#"
		onclicksound =Sound/screens/Button-09.wav

}

[CampaignButton5]
{
type=Button
TwoDArea=0,407,318,50
ToolTip="#iTOOL_135#"
		onclicksound =Sound/screens/Button-09.wav

}

[CampaignButton6]
{
type=Button
TwoDArea=0,500,318,50
ToolTip="#iTOOL_136#"
		onclicksound =Sound/screens/Button-09.wav

}

        [IPAddress]
        {
                type=EditField
                TwoDArea=65,58,0,0
                DefaultText="00.00.00.00"
                Validchars="0123456789."
                MaxLength=15
                Font=serpentine
        }

        [IPAddressText]
        {
                type=Text
                TwoDArea=115,127,0,0
                Text="000.000.000.000"
                Font=serpentine
        }

;	[Client0]
;        {
;                type=Text
;                TwoDArea=600,375,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client1]
;        {
;                type=Text
;                TwoDArea=600,400,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client2]
;        {
;                type=Text
;                TwoDArea=600,425,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client3]
;        {
;                type=Text
;                TwoDArea=600,450,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client4]
;        {
;                type=Text     
;                TwoDArea=600,475,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client5]
;        {
;                type=Text
;                TwoDArea=600,500,0,0
;                Text=""
;                Font=serpentine
;        }
;
;	[Client6]
;        {
;                type=Text
;                TwoDArea=600,525,0,0
;                Text=""
;                Font=serpentine
;        }
;
;
;	[Client7]
;        {
;                type=Text
;                TwoDArea=600,550,0,0
;                Text=""
;                Font=serpentine
;        }

	[IpLstButton0]
        {
                type=Button
                TwoDArea=66,115,238,25
                ToolTip="#iTOOL_137#"
		onclicksound =Sound/screens/Button-09.wav

        }       

	[IpLstText0]
        {
                type=Text
                TwoDArea=66,115,238,25
                Text=""
        }

        [IpLstButton1]
        {
                type=Button
                TwoDArea=66,155,238,25
                ToolTip="#iTOOL_137#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [IpLstText1]
        {
                type=Text
                TwoDArea=66,155,238,25
                Text=""
        }

        [IpLstButton2]
        {
                type=Button
                TwoDArea=66,195,238,25
                ToolTip="#iTOOL_137#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [IpLstText2]
        {
                type=Text
                TwoDArea=66,195,238,25
                Text=""
        }

        [IpLstButton3]
        {
                type=Button
                TwoDArea=66,235,238,25
                ToolTip="#iTOOL_137#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [IpLstText3]
        {
                type=Text
                TwoDArea=66,235,238,25
                Text=""
        }

        [ConnStat]
        {
                type=Text
                TwoDArea=468,436,780,460
                Text=""
		Font=serpentine
        }

        [ParaButton1]
        {
                type=Button
                TwoDArea=17,9,141,54
                ToolTip="#iTOOL_141#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaButton2]
        {
                type=Button
                TwoDArea=20,69,131,46
                ToolTip="#iTOOL_142#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaButton3]
        {
                type=Button
                TwoDArea=10,128,156,46
                ToolTip="#iTOOL_143#"
		onclicksound =Sound/screens/Button-01.wav

        }       

        [ParaButton4]
        {
                type=Button
                TwoDArea=50,181,91,44
                ToolTip="#iTOOL_144#"
		onclicksound =Sound/screens/Button-26.wav

        }       

        [ParaCampaignButton1]
        {
                type=Button
                TwoDArea=548,11,152,24
                ToolTip="#iTOOL_145#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaCampaignButton2]
        {
                type=Button
                TwoDArea=548,45,152,24
                ToolTip="#iTOOL_146#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaCampaignButton3]
        {
                type=Button
                TwoDArea=548,84,159,31
                ToolTip="#iTOOL_147#"
		onclicksound =Sound/screens/Button-09.wav

        }       


        [ParaCampaignButton4]
        {
                type=Button
                TwoDArea=524,133,201,27
                ToolTip="#iTOOL_148#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaCampaignButton5]
        {
                type=Button
                TwoDArea=529,168,190,25
                ToolTip="#iTOOL_149#"
		onclicksound =Sound/screens/Button-09.wav

        }       

        [ParaCampaignButton6]
        {
                type=Button
                TwoDArea=529,201,190,25
                ToolTip="#iTOOL_149a#"
		onclicksound =Sound/screens/Button-09.wav

        }


        [ParaCampaignLoadList]
        {
                type=ListBox
                TwoDArea=220,25,260,190
                ScrollBar=ParaCampaignLoadListScrollBar
        }

        [ParaCampaignLoadListScrollBarUpArrow]
        {
                type=Button
                TwoDArea=485,20,12,12
        }

        [ParaCampaignLoadListScrollBarDownArrow]
        {
                type=Button
                TwoDArea=485,210,12,12
        }

        [ParaCampaignLoadListScrollBar]
        {
                type=ScrollBar
                BarImage=gfx/interface/planetinfo/csuszka.tga
                TwoDArea=488,31,10,178
                Steps=5000
                UpButton=ParaCampaignLoadListScrollBarUpArrow
                DownButton=ParaCampaignLoadListScrollBarDownArrow
		UsesWheel=true
        }

	[ParaScenarioNameField]
	{
		type=EditField
                TwoDArea=270,200,100,25
                Text="#iC_START_13#"
                Font=serpentine
                ToolTip="#iTOOL_188#"
                DefaultText=""
                Validchars="0123456789-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ{}():"
	}


	[ParaScenarioTeamLabel]
	{
		type=Text
                TwoDArea=420,200,0,0
                Text="#iC_START_14#"
                Font=serpentine
	}

	[ParaScenarioTeamButton]
	{
		type=Button
                TwoDArea=420,200,70,30
                ToolTip="#iTOOL_150#"
		onclicksound =Sound/screens/Button-09.wav

	}

        [ParaScenarioEmpireButton]
        {
                type=Button
                TwoDArea=535,200,24,24
                ToolTip="#iTOOL_151#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [ParaScenarioRaceButton]
        {
                type=Button
                TwoDArea=600,200,24,24
                ToolTip="#iTOOL_152#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [ParaScenarioReadyButton]
        {
                type=Button
                TwoDArea=675,200,24,24
                ToolTip="#iTOOL_153#"
		onclicksound =Sound/screens/Button-09.wav

        }


        [ParaScenarioButton0]
        {
                type=Button
                TwoDArea=250,19,360,21
                ToolTip="#iTOOL_154#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [ScenarioLabel]
        {
                type=Text
                TwoDArea=450,23,0,0
                Text=""
                Font=sima
        }

        [ParaScenarioButton1]
        {
                type=Button
                TwoDArea=250,50,260,21
                ToolTip="#iTOOL_155#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [StartingMoneyLabel]
        {
                type=Text
                TwoDArea=450,54,0,0
                Text=""
                Font=sima
        }


        [ParaScenarioButton2]
        {
                type=Button
                TwoDArea=250,82,260,21
                ToolTip="#iTOOL_156#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [RandomEventsLabel]
        {
                type=Text
                TwoDArea=450,87,0,0
                Color=255,255,255,255
                Text=""
                Font=sima
        }

        [ParaScenarioButton3]
        {
                type=Button
                TwoDArea=250,112,260,21
                ToolTip="#iTOOL_157#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [ResearchPointLimitLabel]
        {
                type=Text
                TwoDArea=450,118,0,0
                Text=""
                Font=sima
        }


        [ParaScenarioButton4]
        {
                type=Button
                TwoDArea=250,143,260,21
                ToolTip="#iTOOL_158#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [MineralAbundanceLabel]
        {
                type=Text
                TwoDArea=450,149,0,0
                Text=""
                Font=sima
        }


        [ParaScenarioButton5]
        {
                type=Button
                TwoDArea=555,50,230,21
                ToolTip="#iTOOL_159#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [TechnicalLevelLabel]
        {
                type=Text
                TwoDArea=715,52,0,0
                Color=255,255,255,255
                Text=""
                Font=sima
        }


        [ParaScenarioButton6]
        {
                type=Button
		TwoDArea=555,82,230,21
        ToolTip="#iTOOL_160#"
		onclicksound =Sound/screens/Button-09.wav

        }


        [PauseLabel]
        {
                type=Text
                TwoDArea=715,86,0,0
                Color=255,255,255,255
                Text=""
                Font=sima
        }


        [ParaScenarioButton7]
        {
                type=Button
		TwoDArea=555,116,230,21
        ToolTip="#iTOOL_161#"
		onclicksound =Sound/screens/Button-09.wav

        }


        [AiPlayersLabel]
        {
                type=Text
                TwoDArea=715,118,0,0
                Color=255,255,255,255
                Text=""
                Font=sima
        }


        [ParaScenarioButton8]
        {
                type=Button
		TwoDArea=555,147,230,21
        ToolTip="#iTOOL_162#"
		onclicksound =Sound/screens/Button-09.wav

        }


        [AiLevelLabel]
        {
                type=Text
                TwoDArea=715,149,0,0
                Color=255,255,255,255
                Text=""
                Font=sima
        }


        [ParaScenarioButton9]
        {
                type=Button
		TwoDArea=520,260,260,320
        ToolTip="#iTOOL_163#"
		onclicksound =Sound/screens/Button-09.wav

        }

        [ChatField]
        {
                type=EditField
                TwoDArea=28,567,451,24
                DefaultText=""
                MaxLength=0
		RetainsInputFocus=1
        }

        [ChatList]
        {
                type=ListBox
                TwoDArea=28,243,451,317
                ScrollBar=ScrollBar
        }

        [ScrollBarUpArrow]
        {
                type=Button
                TwoDArea=485,237,12,12
                ToolTip="#iTOOL_04#"
        }

        [ScrollBarDownArrow]
        {
                type=Button
                TwoDArea=485,550,12,12
                ToolTip="#iTOOL_05#"
        }

        [ScrollBar]
        {
                type=ScrollBar
                BarImage=gfx/interface/planetinfo/csuszka.tga
                TwoDArea=488,247,10,303
                Steps=5000
                UpButton=ScrollBarUpArrow
                DownButton=ScrollBarDownArrow
		UsesWheel=true
        }


        [HegeLogo]
        {
                type=Image
                Filename="gfx/interface/start/hegelogo.tga"
                Pos=500,10
        }
        [DemoText]
        {
                type=Text
                Text="#aright#Compatibility demo for Matrox cards#b#Internal use only!"
                TwoDArea=510,560,270,40
                Color=200,200,255,200
        }

        [ConsoleField]
        {
                type=EditField
                TwoDArea=10,10,0,0
                DefaultText=""
                MaxLength=100
                Font=serpentine
        } 
;********************************************************************************************
;***** SM_GS_LOBBY SCREEN *******************************************************************
;********************************************************************************************

        ;[lblLobbyGames]
        ;{
        ;        type=Text
        ;        TwoDArea=16,16,170,30
        ;        Text="Games:"
        ;        Font=serpentine
        ;}


        ;[lblLobbyChat]
        ;{
        ;        type=Text
        ;        TwoDArea=16,270,170,30
        ;        Text="Chat:"
        ;        Font=serpentine
        ;}

        ;[lblLobbyPlayers]
        ;{
        ;        type=Text
        ;        TwoDArea=472,16,70,30
        ;        Text="Players:"
        ;        Font=serpentine
        ;}

        [btnLobbyCreateGame]
        {
                type=Button
                TwoDArea=80,14,170,40
                ToolTip="#iTOOL_164#"
		onclicksound =Sound/screens/Button-09.wav

	}

        [btnLobbyJoinGame]
        {
                type=Button
                TwoDArea=80,100,170,40
                ToolTip="#iTOOL_165#"
		onclicksound =Sound/screens/Button-09.wav

	}

        [btnLobbyBack]
        {
                type=Button
                TwoDArea=80,270,170,40
                ToolTip="#iTOOL_166#"
;		onclicksound =Sound/screens/Button-37.wav

	}

        ;[lblLobbyCreateGame]
        ;{
        ;        type=Text
	;	Align=ALIGN_CENTER
        ;        TwoDArea=544,320,170,40
        ;        Text="Create Game"
        ;        Font=serpentine
	;}

        ;[lblLobbyJoinGame]
        ;{
        ;        type=Text
;		Align=ALIGN_CENTER
;                TwoDArea=544,384,170,40
;                Text="Join Game"
;                Font=serpentine
;	}

;        [lblLobbyBack]
;        {
;                type=Text
;		Align=ALIGN_CENTER
;	        TwoDArea=544,504,170,40
;                Text="Back"
;                Font=serpentine
;	}

;***** Games listbox ************************************************************************

;	[imgLobbyGamesBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lobbygameslist.tga
;		Pos=16,64
;	}

;	[imgLobbyGamesScrollUpBack];
;	{
;		type=Image
;		Filename=gfx/prism/start/felnyil.tga
;		Pos=776,19		
;	}


	[btnLobbyGamesScrollUp]
	{
		type=Button
		TwoDArea=778,19,10,10
;		Children = imgLobbyGamesScrollDownBack
ToolTip="#iTOOL_167#"
	}

;	[imgLobbyGamesScrollDownBack]
;	{;
;		type=Image
;		Filename=gfx/prism/start/lenyil.tga
;		Pos=446,244
;	}

	[btnLobbyGamesScrollDown]
	{
		type=Button
		TwoDArea=778,328,10,10
;		Children = imgLobbyGamesScrollUpBack
ToolTip="#iTOOL_168#"
	}


	[scbLobbyGamesScrollBar]
	{
		type=ScrollBar
		TwoDArea=780,25,10,306
                BarImage=gfx/interface/planetinfo/csuszka.tga
		UpButton=btnLobbyGamesScrollUp
		DownButton=btnLobbyGamesScrollDown
		Children=btnLobbyGamesScrollDown,btnLobbyGamesScrollUp
	}

	[lstLobbyGames]
	{
		type=ListBox
		TwoDArea=420,28,354,304
;		BackGround = imgLobbyGamesBack
		Scrollbar=scbLobbyGamesScrollBar
		Children=scbLobbyGamesScrollBar
	}

;***** Games listbox end ********************************************************************

;***** Chat listbox *************************************************************************

;	[imgLobbyChatBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lobbygameslist.tga
;		Pos=16,320
;	}

;	[imgLobbyChatScrollUpBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/felnyil.tga
;		Pos=446,320
;	}

	[btnLobbyChatScrollUp]
	{
		type=Button
		TwoDArea=486,354,10,10
;		Children = imgLobbyChatScrollUpBack
ToolTip="#iTOOL_169#"
	}

;	[imgLobbyChatScrollDownBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lenyil.tga
;		Pos=446,500
;	}


	[btnLobbyChatScrollDown]
	{
		type=Button
		TwoDArea=486,542,10,10
;		Children = imgLobbyChatScrollDownBack
ToolTip="#iTOOL_170#"
	}


	[scbLobbyChatScrollBar]
	{
		type=ScrollBar
		TwoDArea=488,366,10,175
                BarImage=gfx/interface/planetinfo/csuszka.tga
		UpButton=btnLobbyChatScrollUp
		DownButton=btnLobbyChatScrollDown
		Children=btnLobbyChatScrollDown,btnLobbyChatScrollUp
	}

	[lstLobbyChat]
	{
		type=ListBox
		TwoDArea=24,364,440,188
;		BackGround = imgLobbyChatBack
		Scrollbar=scbLobbyChatScrollBar
		Children=scbLobbyChatScrollBar
	}

;***** Chat listbox end *********************************************************************

;***** Player listbox ***********************************************************************

;	[imgLobbyPlayersBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lobbygameslist.tga
;		Pos=16,320
;	}

;	[imgLobbyPlayersScrollUpBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/felnyil.tga
;		Pos=762,64
;	}

	[btnLobbyPlayersScrollUp]
	{
		type=Button
		TwoDArea=778,354,10,10
;		Children = imgLobbyPlayersScrollUpBack
ToolTip="#iTOOL_171#"
	}

;	[imgLobbyPlayersScrollDownBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lenyil.tga
;		Pos=762,244
;	}

	[btnLobbyPlayersScrollDown]
	{
		type=Button
		TwoDArea=778,580,10,10
;		Children = imgLobbyPlayersScrollDownBack
ToolTip="#iTOOL_172#"
	}


	[scbLobbyPlayersScrollBar]
	{
		type=ScrollBar
		TwoDArea=780,366,10,226
                BarImage=gfx/interface/planetinfo/csuszka.tga
		UpButton=btnLobbyPlayersScrollUp
		DownButton=btnLobbyPlayersScrollDown
		Children=btnLobbyPlayersScrollDown,btnLobbyPlayersScrollUp
	}

	[lstLobbyPlayers]
	{
		type=ListBox
		TwoDArea=516,364,290,226
;		BackGround = imgLobbyPlayersBack
		Scrollbar=scbLobbyPlayersScrollBar
		Children=scbLobbyPlayersScrollBar
	}

;***** Player listbox end *******************************************************************

	[edtLobbyChat]
	{
		type = EditField
                TwoDArea = 24,564,440,30
		RetainsInputFocus=1
                DefaultText=""
	}


;********************************************************************************************
;***** SM_GS_LOBBY SCREEN END ***************************************************************
;********************************************************************************************


;********************************************************************************************
;***** SM_GS_ROOM SCREEN ********************************************************************
;********************************************************************************************

;        [lblRoomPlayers]
;        {
;                type=Text
;                TwoDArea=16,16,170,30
;                Text="Players:"
;                Font=serpentine
;        }

;        [lblRoomChat]
;        {
;                type=Text
;                TwoDArea=16,270,170,30
;                Text="Chat:"
;                Font=serpentine
;        }

        [btnRoomReady]
        {
                type=Button
                TwoDArea=80,14,170,40
                ToolTip="#iTOOL_173#"
		onclicksound =Sound/screens/Button-09.wav

	}

        [btnRoomStart]
        {
                type=Button
                TwoDArea=80,100,170,40
                ToolTip="#iTOOL_174#"
		onclicksound =Sound/screens/Button-01.wav

	}

        [btnRoomBack]
        {
                type=Button
                TwoDArea=80,274,170,40
                ToolTip="#iTOOL_175#"
;		onclicksound =Sound/screens/Button-37.wav

	}

;        [lblRoomReady]
;        {
;                type=Text
;		Align=ALIGN_CENTER
;                TwoDArea=544,320,170,40
;                Text="Ready"
;                Font=serpentine
;	}

;        [lblRoomStart]
;        {
;                type=Text
;		Align=ALIGN_CENTER
;                TwoDArea=544,384,170,40
;                Text="Start"
;                Font=serpentine
;	}

;        [lblRoomBack]
;        {
;                type=Text
;		Align=ALIGN_CENTER
;	        TwoDArea=544,504,170,40
;                Text="Back"
;                Font=serpentine
;	}

;***** Players listbox **********************************************************************

;	[imgLobbyGamesBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lobbygameslist.tga
;		Pos=16,64
;	}

;	[imgRoomPlayersScrollUpBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/felnyil.tga
;		Pos=762,64
;	}


	[btnRoomPlayersScrollUp]
	{
		type=Button
		TwoDArea=778,352,10,10
;		Children = imgRoomPlayersScrollDownBack
ToolTip="#iTOOL_176#"
	}

;	[imgRoomPlayersScrollDownBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lenyil.tga
;		Pos=762,244
;	}

	[btnRoomPlayersScrollDown]
	{
		type=Button
		TwoDArea=778,580,10,10
;		Children = imgRoomPlayersScrollUpBack
ToolTip="#iTOOL_177#"
	}


	[scbRoomPlayersScrollBar]
	{
		type=ScrollBar
		TwoDArea=781,364,10,218
                BarImage=gfx/interface/planetinfo/csuszka.tga
		UpButton=btnRoomPlayersScrollUp
		DownButton=btnRoomPlayersScrollDown
		Children=btnRoomPlayersScrollDown,btnRoomPlayersScrollUp
	}

	[lstRoomPlayers]
	{
		type=ListBox
		TwoDArea=520,364,256,224
;		BackGround = imgLobbyGamesBack
		Scrollbar=scbRoomPlayersScrollBar
		Children=scbRoomPlayersScrollBar
	}

;***** Players listbox end ******************************************************************

;***** Chat listbox *************************************************************************

;	[imgLobbyChatBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lobbygameslist.tga
;		Pos=16,320
;	}

;	[imgRoomChatScrollUpBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/felnyil.tga
;		Pos=446,320
;	}

	[btnRoomChatScrollUp]
	{
		type=Button
		TwoDArea=486,352,10,10
;		Children = imgRoomChatScrollUpBack
ToolTip="#iTOOL_178#"
	}

;	[imgRoomChatScrollDownBack]
;	{
;		type=Image
;		Filename=gfx/prism/start/lenyil.tga
;		Pos=446,500
;	}


	[btnRoomChatScrollDown]
	{
		type=Button
		TwoDArea=486,544,10,10
;		Children = imgRoomChatScrollDownBack
ToolTip="#iTOOL_179#"
	}


	[scbRoomChatScrollBar]
	{
		type=ScrollBar
		TwoDArea=488,364,10,178
                BarImage=gfx/interface/planetinfo/csuszka.tga
		UpButton=btnRoomChatScrollUp
		DownButton=btnRoomChatScrollDown
		Children=btnRoomChatScrollDown,btnRoomChatScrollUp
	}

	[lstRoomChat]
	{
		type=ListBox
		TwoDArea=24,364,454,190
;		BackGround = imgLobbyChatBack
		Scrollbar=scbRoomChatScrollBar
		Children=scbRoomChatScrollBar
	}

;***** Chat listbox end *********************************************************************

	[edtRoomChat]
	{
		type = EditField
                TwoDArea = 24,564,454,30
		RetainsInputFocus=1
                DefaultText=""
	}


;********************************************************************************************
;***** SM_GS_ROOM SCREEN END ****************************************************************
;********************************************************************************************
	[ScenarioPopup]
	{
		type=PopupMenu
                TwoDArea=450,46,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
		Text="Teszt1"
		Text="Testz2"
	} 

	[StartingMoneyPopup]
	{
		type=PopupMenu
                TwoDArea=450,77,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_RIGHT
		Text="0"
		Text="500"
		Text="1000"
		Text="2000"
		Text="5000"
		Text="10000"
	} 

	[RandomEventsPopup]
	{
		type=PopupMenu
                TwoDArea=450,110,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_15#"
                Text="#iC_START_16#"
                Text="#iC_START_17#"
                Text="#iC_START_18#"

	} 

	[ResearchPointLimitPopup]
	{
		type=PopupMenu
                TwoDArea=450,141,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_RIGHT
		Text="5000"
		Text="10000"
		Text="20000"
		Text="40000"
		Text="60000"
                Text="#iC_START_19#"

	} 

	[MineralAbundancePopup]
	{
		type=PopupMenu
                TwoDArea=450,172,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_20#"
                Text="#iC_START_21#"
                Text="#iC_START_22#"
		onclicksound =Sound/screens/Button-35.wav

	} 

	[TechnicalLevelPopup]
	{
		type=PopupMenu
                TwoDArea=715,76,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_23#"
                Text="#iC_START_24#"
                Text="#iC_START_25#"
                Text="#iC_START_26#"
                Text="#iC_START_27#"

	} 


	[PausePopup]
	{
		type=PopupMenu
                TwoDArea=715,110,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_28#"
                Text="#iC_START_29#"
                Text="#iC_START_30#"

	} 

	[AiPlayersPopup]
	{
		type=PopupMenu
                TwoDArea=715,142,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
		Text="0"
		Text="1"
		Text="2"
		Text="3"
		Text="4"
		Text="5"
		Text="6"
		Text="7"
	} 

	[AiLevelPopup]
	{
		type=PopupMenu
                TwoDArea=715,173,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_31#"
                Text="#iC_START_32#"
                Text="#iC_START_33#"

	} 

	[ParaScenarioTeamPopup]
	{
		type=PopupMenu
                TwoDArea=420,230,0,0
                Font=serpentine
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_34#"
                Text="#iC_START_35#"
                Text="#iC_START_36#"
                Text="#iC_START_37#"
                Text="#iC_START_38#"
                Text="#iC_START_39#"
                Text="#iC_START_40#"
                Text="#iC_START_41#"

	} 

	[ParaScenarioEmpirePopup]
	{
		type=PopupMenu
                TwoDArea=535,230,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		;Image=gfx/interface/ikonok/multiplayer1/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer2/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer3/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer4/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer5/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer6/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer7/multiplayer_ikon.tga
		;Image=gfx/interface/ikonok/multiplayer8/multiplayer_ikon.tga
	} 

	[ParaScenarioRacePopup]
	{
		type=PopupMenu
                TwoDArea=600,230,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Image=GFX/Interface/starmap/faj1.tga
		Image=GFX/Interface/starmap/faj2.tga
		Image=GFX/Interface/starmap/faj3.tga
	} 

	[KickPopup]
	{
		type=PopupMenu
                TwoDArea=0,0,0,0
                Font=sima
		LeftButtonWorks=1
                DrawBorder=1
                AutoWidth=1
		Align=ALIGN_LEFT
                Text="#iC_START_KICK#"
	} 


        [CoverButton]
        {
                type=Button
                TwoDArea=0,0,800,600
        }

        [CdKeyImage]
        {
                type=Image
                Filename="gfx/interface/start/cdkey.tga"
                Pos=278,200
        }

        [CdKeyText]
        {
                type=Text
		Text="#iC_START_INVCDK#"
                TwoDArea=285,270,229,30
		Color=200,0,0,255
                Align=ALIGN_CENTER
                Font=serpentine
        }


        [CdKeyField]
        {
                type=EditField
                TwoDArea=285,369,229,30
                DefaultText=""
                Validchars="0123456789-abcdefABCDEF"
                MaxLength=0
                Font=serpentine
                Align=ALIGN_LEFT
        }

        [GameSpyNameImage]
        {
                type=Image
                Filename="gfx/interface/start/gamespyname.tga"
                Pos=210,200
        }

        [GameSpyNameField]
        {
                type=EditField
                TwoDArea=215,369,370,30
                DefaultText=""
                Validchars="0123456789-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ{}():"
                MaxLength=11
                Font=job
                Align=ALIGN_LEFT
        }

        [GamespyLogo1]
        {
                type=Image
                Filename="gfx/interface/start/gamespy.tga"
                Pos=639,298
        }

        [GamespyLogo2]
        {
                type=Image
                Filename="gfx/interface/start/gamespy.tga"
                Pos=639,21
        }

        [GamespyLogo3]
        {
                type=Image
                Filename="gfx/interface/start/gamespy.tga"
                Pos=624,530
        }

}
