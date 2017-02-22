(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('UserConnectionDetailController', UserConnectionDetailController);

    UserConnectionDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'UserConnection', 'User'];

    function UserConnectionDetailController($scope, $rootScope, $stateParams, previousState, entity, UserConnection, User) {
        var vm = this;

        vm.userConnection = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('backendApp:userConnectionUpdate', function(event, result) {
            vm.userConnection = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
