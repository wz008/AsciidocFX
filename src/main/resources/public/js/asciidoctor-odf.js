/* Generated by Opal 0.6.3 */
(function ($opal) {
    var self = $opal.top, $scope = $opal, nil = $opal.nil, $breaker = $opal.breaker, $slice = $opal.slice, $module = $opal.module, $klass = $opal.klass, $hash2 = $opal.hash2;

    return (function ($base) {
        var self = $module($base, 'Asciidoctor');

        var def = self._proto, $scope = self._scope;

        (function ($base, $super) {
            function $OdfConverter() {
            };
            var self = $OdfConverter = $klass($base, $super, 'OdfConverter', $OdfConverter);

            var def = self._proto, $scope = self._scope;

            return (def.$convert = function (node, template_name, opts) {
                    var $a, self = this;

                    var obj = {};
                    obj.type = node.$type ? node.$type() : "";
                    obj.name = node.$node_name ? node.$node_name() : "";
                    obj.content = node.$content ? node.$content() : "";
                    obj.text = node.$text ? node.$text() : "";

                    app.convertToOdf(obj.content);

                    if (template_name == null) {
                        template_name = nil
                    }
                    if (opts == null) {
                        opts = $hash2([], {})
                    }
                    return nil;
                }, nil) && 'convert'
        })($scope.Converter, ($scope.Converter)._scope.Base)

    })(self)
})(Opal);