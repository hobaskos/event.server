(function() {
    'use strict';
    angular
        .module('backendApp')
        .factory('Location', Location);

    Location.$inject = ['$resource', 'DateUtils'];

    function Location ($resource, DateUtils) {
        var resourceUrl =  'api/locations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.fromDate = DateUtils.convertDateTimeFromServer(data.fromDate);
                        data.toDate = DateUtils.convertDateTimeFromServer(data.toDate);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
