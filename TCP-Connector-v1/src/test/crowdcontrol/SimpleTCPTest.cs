using System;
using System.Collections.Generic;
using CrowdControl.Common;
using CrowdControl.Games.Packs;
using ConnectorType = CrowdControl.Common.ConnectorType;

public class SimpleTCPTest : SimpleTCPPack{
    public override string Host => "127.0.0.1";
    public override ushort Port => 58430;

    public SimpleTCPTest(IPlayer player, Func<CrowdControlBlock, bool> responseHandler, Action<object> statusUpdateHandler)
        : base(player,responseHandler,statusUpdateHandler){}

    public override Game Game => new Game(42, "LCCrowdControlTestTCP","testtcp","PC", ConnectorType.SimpleTCPConnector);

    public override List<Effect> Effects => new List<Effect>{
        new Effect("Test","test1"),
        new Effect("FailureTest","failure_test1")
    };
}