(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('EventPoll', EventPoll);

    EventPoll.$inject = ['$resource'];

    function EventPoll ($resource) {
        var resourceUrl =  'api/event-polls/:id';

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
