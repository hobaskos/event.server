(function() {
    'use strict';

    angular
        .module('backendApp')
        .factory('UserConnectionSearch', UserConnectionSearch);

    UserConnectionSearch.$inject = ['$resource'];

    function UserConnectionSearch($resource) {
        var resourceUrl =  'api/_search/user-connections/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true}
        });
    }
})();
