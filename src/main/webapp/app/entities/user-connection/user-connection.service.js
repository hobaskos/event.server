(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('UserConnection', UserConnection);

    UserConnection.$inject = ['$resource'];

    function UserConnection ($resource) {
        var resourceUrl =  'api/user-connections/:id';

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
