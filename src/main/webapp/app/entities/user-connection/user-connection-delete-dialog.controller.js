(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('UserConnectionDeleteController',UserConnectionDeleteController);

    UserConnectionDeleteController.$inject = ['$uibModalInstance', 'entity', 'UserConnection'];

    function UserConnectionDeleteController($uibModalInstance, entity, UserConnection) {
        var vm = this;

        vm.userConnection = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            UserConnection.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
