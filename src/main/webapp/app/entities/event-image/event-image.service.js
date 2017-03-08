(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('EventImage', EventImage);

    EventImage.$inject = ['$resource'];

    function EventImage ($resource) {
        var resourceUrl =  'api/event-images/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
