(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventImageVoteDeleteController',EventImageVoteDeleteController);

    EventImageVoteDeleteController.$inject = ['$uibModalInstance', 'entity', 'EventImageVote'];

    function EventImageVoteDeleteController($uibModalInstance, entity, EventImageVote) {
        var vm = this;

        vm.eventImageVote = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            EventImageVote.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
