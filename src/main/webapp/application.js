(function($)
{
    var cometd = $.cometd;

    var dataSet = new TimeSeries();

    $(document).ready(function()
    {
        function _connectionEstablished()
        {
            $('#body').append('<div>CometD Connection Established</div>');
        }

        function _connectionBroken()
        {
            $('#body').append('<div>CometD Connection Broken</div>');
        }

        function _connectionClosed()
        {
            $('#body').append('<div>CometD Connection Closed</div>');
        }

        // Function that manages the connection status with the Bayeux server
        var _connected = false;
        function _metaConnect(message)
        {
            if (cometd.isDisconnected())
            {
                _connected = false;
                _connectionClosed();
                return;
            }

            var wasConnected = _connected;
            _connected = message.successful === true;
            if (!wasConnected && _connected)
            {
                _connectionEstablished();
            }
            else if (wasConnected && !_connected)
            {
                _connectionBroken();
            }
        }

        // Function invoked when first contacting the server and
        // when the server has lost the state of this client
        function _metaHandshake(handshake)
        {
            if (handshake.successful === true)
            {
                cometd.batch(function()
                {
                    cometd.subscribe('/hello', function(message)
                    {
                        if(message.data.greeting) {
                            $('#body').append('<div>Server Says: ' + message.data.greeting + '</div>');
                        }

                        if(message.data.time  && message.data.value) {
                            var date = Date.parse(message.data.time);
                            dataSet.append(date, parseFloat(message.data.value));
                            $('#body').append("<div>time: " +  date + ", value: " + message.data.value + "</div>");
                        }
                    });
                    // Publish on a service channel since the message is for the server only
                    cometd.publish('/service/hello', { name: 'Fred' });
                });
            }
        }

        // Disconnect when the page unloads
        $(window).unload(function()
        {
            cometd.disconnect(true);
        });

        ///////////////////
        var smoothie = new SmoothieChart({ minValue: -1.0, maxValue: 1.0, millisPerPixel: 20, grid: { strokeStyle: '#555555', lineWidth: 1, millisPerLine: 250, verticalSections: 6 }});
        smoothie.addTimeSeries(dataSet, { strokeStyle: 'rgba(255, 0, 0, 1)', fillStyle: 'rgba(255, 0, 0, 0.2)', lineWidth: 3 });
        smoothie.streamTo(document.getElementById('chart'), 1000);
        ///////////////////

        var cometURL = location.protocol + "//" + location.host + config.contextPath + "/cometd";
        cometd.configure({
            url: cometURL,
            logLevel: 'debug'
        });

        cometd.addListener('/meta/handshake', _metaHandshake);
        cometd.addListener('/meta/connect', _metaConnect);

        cometd.handshake();
    });
})(jQuery);
