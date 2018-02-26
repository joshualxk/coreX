(function (ctx) {

    const BODY_TYPE_STRUCT = 1;

    var RpcRequest;
    var RpcResponse;
    var Push;
    var ClientPayload;
    var Struct;

    var RPC = {};

    RPC.init = function (callback) {
        protobuf.load("proto/model.proto", function (err, root) {
            if (err) {
                callback(err);
                return;
            }

            RpcRequest = root.lookup("corex.proto.RpcRequest");
            RpcResponse = root.lookup("corex.proto.RpcResponse");
            Push = root.lookup("corex.proto.Push");
            ClientPayload = root.lookup("corex.proto.ClientPayload");

            Struct = root.lookup("corex.proto.Struct");

            if (RpcRequest == null
                || RpcResponse == null
                || Push == null
                || ClientPayload == null
                || Struct == null) {
                callback(new Error("proto not found"));
                return;
            }

            callback();
        });
    };

    function is(obj, type) {
        var toString = Object.prototype.toString;
        return (type === "Null" && obj === null) ||
            (type === "Undefined" && obj === undefined) ||
            toString.call(obj).slice(8, -1) === type;
    }

    function encodeStruct(target) {
        var obj = {};
        for (var k in target) {
            obj[k] = encodeValue(target[k]);
        }

        return {fields: obj};
    }

    function encodeListValue(target) {
        var arr = [];
        for (var k in target) {
            arr.push(encodeValue(target[k]));
        }
        return {listValue: arr};
    }

    function encodeValue(target) {
        if (target === null || typeof target === 'function') {
            return null;
        }
        if (typeof target === 'boolean') {
            return {boolValue: target};
        } else if (typeof target === 'string') {
            return {stringValue: target};
        } else if (typeof target === 'number') {
            // TODO 只支持int型
            return {intValue: target};
        } else if (is(target, 'Array')) {
            return {listValue: encodeListValue(target)};
        } else if (is(target, 'Object')) {
            return {structValue: encodeStruct(target)};
        }

        return null;
    }

    function decodeStruct(target) {
        var obj = {};
        for (var k in target.fields) {
            obj[k] = decodeValue(target.fields[k]);
        }
        return obj;
    }

    function decodeListValue(target) {
        var arr = [];
        for (var k in target['values']) {
            arr.push(decodeValue(target['values'][k]));
        }
        return arr;
    }

    function decodeValue(target) {
        if (target.hasOwnProperty('stringValue')) {
            return target['stringValue'];
        } else if (target.hasOwnProperty('intValue')) {
            return target['intValue'];
        } else if (target.hasOwnProperty('longValue')) {
            return target['longValue'];
        } else if (target.hasOwnProperty('doubleValue')) {
            return target['doubleValue'];
        } else if (target.hasOwnProperty('boolValue')) {
            return target['boolValue'];
        } else if (target.hasOwnProperty('structValue')) {
            return decodeStruct(target['structValue']);
        } else if (target.hasOwnProperty('listValue')) {
            return decodeListValue(target['listValue']);
        }
        return null;
    }

    RPC.wrapMsg = function (msg) {
        if (!is(msg, 'Object')) {
            throw new Error("msg is not Object");
        }
        return encodeStruct(msg);
    };

    RPC.unwrapMsg = function (msg) {
        if (msg['fields'] === undefined) {
            throw new Error("msg is not wrapped msg");
        }

        return decodeStruct(msg);
    };

    RPC.buildPayload = function (id, module, api, token, param) {

        var struct = RPC.wrapMsg(param);
        // logger.log(JSON.stringify(struct));
        var buffer = Struct.encode(Struct.create(struct)).finish();

        var rpcRequest = {
            id: id,
            method: {module: module, api: api},
            auth: {type: 0, token: token},
            timestamp: +new Date(),
            body: {
                type: BODY_TYPE_STRUCT,
                body: buffer
            }
        };

        var payload = {rpcRequest: rpcRequest};

        var buffer2 = ClientPayload.encode(ClientPayload.create(payload)).finish();

        // logger.log(JSON.stringify(Struct.decode(buffer)));

        return buffer2;
    };

    RPC.decode = function (resp) {
        var ret = ClientPayload.decode(new Uint8Array(resp));

        if (ret.hasOwnProperty('rpcResponse')) {
            var rpcResponse = ret['rpcResponse'];

            if (rpcResponse.hasOwnProperty('body')) {
                if (rpcResponse.body.type !== BODY_TYPE_STRUCT) {
                    throw new Error("not supported type:" + rpcResponse.body.type);
                }

                var body = Struct.decode(new Uint8Array(rpcResponse.body.body));
                rpcResponse.body = RPC.unwrapMsg(body);
            }
        } else if (ret.hasOwnProperty('push')) {
            var push = ret['push'];

            if (push.hasOwnProperty('body')) {
                if (push.body.type !== BODY_TYPE_STRUCT) {
                    throw new Error("not supported type:" + push.body.type);
                }

                var body = Struct.decode(new Uint8Array(push.body.body));
                push.body = RPC.unwrapMsg(body);
            }
        }

        return ret;

    };

    ctx.RPC = RPC;

})(window);
