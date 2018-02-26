(function (ctx) {

    var logger = {};

    var idCount = 0;

    logger.log = function () {
        var id = idCount++;
        console.log("<<<<<<<<<<<<<<<<<<<<<<" + id);
        console.log.apply(this, arguments);
        console.log(">>>>>>>>>>>>>>>>>>>>>>" + id);
    };

    ctx.logger = logger;
})(window);
